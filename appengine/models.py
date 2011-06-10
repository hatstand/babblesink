from google.appengine.api import users
from google.appengine.ext import db

class Device(db.Model):
  registration_id = db.StringProperty(required=True)
  user = db.UserProperty(required=True)
  brand = db.StringProperty()
  device = db.StringProperty()
  manufacturer = db.StringProperty()
  model = db.StringProperty()
