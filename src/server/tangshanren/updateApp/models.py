from django.db import models

# Create your models here.
class Version(models.Model):
    num = models.CharField(max_length=20, default="0.0.1")
    num_type = models.IntegerField()

    def __unicode__(self):
    	return self.num
