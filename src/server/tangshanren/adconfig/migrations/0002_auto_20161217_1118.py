# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('adconfig', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='ad',
            name='url',
            field=models.URLField(max_length=100),
        ),
    ]
