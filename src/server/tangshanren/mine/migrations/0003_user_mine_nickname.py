# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mine', '0002_auto_20161217_1245'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_mine',
            name='nickname',
            field=models.CharField(default=0, max_length=30),
            preserve_default=False,
        ),
    ]
