# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('spirit_topic_notification', '0002_auto_20150828_2003'),
    ]

    operations = [
        migrations.AlterField(
            model_name='topicnotification',
            name='is_active',
            field=models.BooleanField(default=True),
        ),
    ]
