# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('live', '0004_auto_20161022_2153'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_session',
            name='local',
            field=models.IntegerField(default=0),
            preserve_default=False,
        ),
    ]
