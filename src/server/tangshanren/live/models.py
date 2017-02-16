from django.db import models

# Create your models here.
class user_session(models.Model):
    title = models.CharField(max_length=10)
    auth_id = models.IntegerField()
    playCount = models.IntegerField()
    cover = models.CharField(max_length=255)
    session_id = models.CharField(max_length=100)
    session_push = models.CharField(max_length=100)
    session_pull = models.CharField(max_length=100)
    room_id = models.CharField(max_length=100)
    chat_id = models.CharField(max_length=100)
    istate = models.IntegerField()
    local = models.IntegerField()

    def __unicode__(self):
    	return self.title