from django.db import models
from django.conf import settings
from django.utils import timezone

# Create your models here.
class VirAccount(models.Model):
    userid = models.PositiveIntegerField(("user_id"), default=0)
    username = models.CharField(max_length=10, default="em.")
    balance = models.PositiveIntegerField(("balance"), default=0)
    is_locked = models.BooleanField(("locked"), default=False)
    last_active = models.DateTimeField(("last_active"), default=timezone.now)

    def __unicode__(self):
    	return self.balance
