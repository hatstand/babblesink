from google.appengine.api import urlfetch
from google.appengine.api import users
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

from django.utils import simplejson

import logging
import os
import random
import urllib

import models

CLIENTLOGIN_URL = 'https://www.google.com/accounts/ClientLogin'
C2DM_URL = 'https://android.apis.google.com/c2dm/send'

C2DM_PASSWORD = 'foobar'

class RegisterDevice(webapp.RequestHandler):
  def post(self):
    user = users.get_current_user()
    registration_id = self.request.get('registration_id')
    brand = self.request.get('brand')
    device = self.request.get('device')
    manufacturer = self.request.get('manufacturer')
    model = self.request.get('model')

    if registration_id is None:
      self.response.out.write('No registration id')
      self.error(400)
      return

    d = models.Device(
        registration_id=registration_id,
        user=user,
        brand=brand,
        device=device,
        manufacturer=manufacturer,
        model=model)
    d.put()
    self.response.out.write('OK')


class ListDevices(webapp.RequestHandler):
  def get(self):
    user = users.get_current_user()
    query = models.Device.gql('WHERE user = :user', user=user)
    devices = query.fetch(10)
    path = os.path.join(os.path.dirname(__file__), 'devices.html')
    self.response.out.write(template.render(path, {'devices': devices}))


class PingDevice(webapp.RequestHandler):
  def post(self):
    user = users.get_current_user()
    reg_id = self.request.get('registration_id')
    logging.debug('Request: %s', self.request.body)
    if reg_id is None:
      self.error(400)
      return
    logging.debug('Registration id: %s', reg_id)
    params = {
      'accountType': 'HOSTED',
      'Email': 'c2dm@clementine-player.org',
      'Passwd': C2DM_PASSWORD,
      'service': 'ac2dm',
      'source': 'com.purplehatstands.babblesink',
    }
    logging.debug('ClientLogin params: %s', params)
    params_string = urllib.urlencode(params)
    url = CLIENTLOGIN_URL
    result = urlfetch.fetch(url=url, payload=params_string, method=urlfetch.POST)
    if result.status_code != 200:
      self.error(500)
      logging.error('Error authenticating to ClientLogin: %s', result.content)
      return

    for line in result.content.split('\n'):
      mapping = line.split('=')
      if mapping[0] == 'Auth':
        auth_token = mapping[1]
        break;

    logging.info('Got ClientLogin token: %s', auth_token)
    c2dm_params = {
      'registration_id': reg_id,
      'collapse_key': str(random.random()),
      'data.method': 'whereareyou',
    }
    logging.debug('C2DM params: %s', c2dm_params)
    c2dm_params_string = urllib.urlencode(c2dm_params)
    response = urlfetch.fetch(
        url=C2DM_URL,
        payload=c2dm_params_string,
        method=urlfetch.POST,
        headers={'Authorization': 'GoogleLogin auth=' + auth_token})
    if response.status_code != 200:
      logging.error('Failed to send c2dm message: %s', response.content)

    logging.info(response.content)


application = webapp.WSGIApplication(
    [
        (r'/c2dm/register', RegisterDevice),
        (r'/c2dm/list', ListDevices),
        (r'/c2dm/ping', PingDevice),
    ],
    debug=True)

def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
