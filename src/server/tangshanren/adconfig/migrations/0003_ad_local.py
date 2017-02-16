# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('adconfig', '0002_auto_20161217_1118'),
    ]

    operations = [
        migrations.AddField(
            model_name='ad',
            name='local',
            field=models.CharField(default=0, max_length=100),
            preserve_default=False,
        ),
    ]
