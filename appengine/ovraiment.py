from google.appengine.api import channel
from google.appengine.api import memcache
from google.appengine.api import users

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

from django.utils import simplejson

import random


class ChannelRequestPage(webapp.RequestHandler):
  def get(self):
    user = users.get_current_user()
    channel_id = str(random.random())
    token = channel.create_channel(channel_id)
    memcache.set(user.email(), channel_id)
    self.response.out.write(token)


class ChannelPingPage(webapp.RequestHandler):
  def post(self):
    user = users.get_current_user()
    channel_id = memcache.get(user.email())
    if channel_id is None:
      self.error(403)
      return

    channel.send_message(channel_id, 'Hello, World!')
    self.response.out.write('OK')


class PhoneRinging(webapp.RequestHandler):
  def post(self):
    user = users.get_current_user()
    channel_id = memcache.get(user.email())
    if channel_id is None:
      self.error(403)
      return

    update = {
      'phone_state': 'ringing',
      'incoming_number': self.request.get('number')
    }
    channel.send_message(channel_id, simplejson.dumps(update))
    self.response.out.write('OK')


application = webapp.WSGIApplication(
  [
    (r'/et', ChannelRequestPage),
    (r'/et/ping', ChannelPingPage),
    (r'/et/phone', PhoneRinging),
  ],
  debug=True)

def main():
  run_wsgi_app(application)

if __name__ == '__main__':
  main()
