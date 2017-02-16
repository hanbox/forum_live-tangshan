# -*- coding: utf-8 -*-
from django.db import models
from django.conf import settings
from django.utils import timezone

cpunum_choice = ( 
    ('', u"---------"), 
    (2, u"2"),         
    (4, u"4"),         
    (8, u"8"), 
    (16, u"16"), 
) 
# Create your models here.
class Ad(models.Model):
    zone_id = models.IntegerField()
    title = models.CharField(max_length=100)
    url = models.URLField(max_length=100)
    istate = models.IntegerField()
    itype = models.IntegerField()
    index = models.IntegerField()  
    local = models.CharField(max_length=100)
    imgp = models.ImageField(upload_to='imgpath',null=False,blank=True) 
    last_active = models.DateTimeField(("last_active"), default=timezone.now)

    def __unicode__(self):
        return self.title