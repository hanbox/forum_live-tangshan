from django.db import models

# Create your models here.
class user_session(models.Model):
    title = models.CharField(max_length=10)
    playCount = models.IntegerField()
    cover = models.CharField(max_length=255)

    def __unicode__(self):
    	return self.title