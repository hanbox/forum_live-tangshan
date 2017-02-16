# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('vircurrency', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='viraccount',
            name='username',
            field=models.CharField(default=b'em.', max_length=10),
        ),
        migrations.AlterField(
            model_name='viraccount',
            name='userid',
            field=models.PositiveIntegerField(default=0, verbose_name=b'user_id'),
        ),
    ]
