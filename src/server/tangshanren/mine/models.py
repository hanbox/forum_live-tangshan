from django.db import models

# Create your models here.
class user_mine(models.Model):
    username = models.CharField(max_length=10)
    locol_id = models.IntegerField()
    phone = models.CharField(max_length=11)
    nickname = models.CharField(max_length=30)
    headimgurl = models.CharField(max_length=100)
    login_src = models.CharField(max_length=10)

    def __unicode__(self):
    	return self.username