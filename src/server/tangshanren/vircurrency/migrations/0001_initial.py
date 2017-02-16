# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.utils.timezone
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='VirAccount',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('balance', models.PositiveIntegerField(default=0, verbose_name=b'balance')),
                ('is_locked', models.BooleanField(default=False, verbose_name=b'locked')),
                ('last_active', models.DateTimeField(default=django.utils.timezone.now, verbose_name=b'last_active')),
                ('userid', models.ForeignKey(related_name='st_viraccount', to=settings.AUTH_USER_MODEL)),
            ],
        ),
    ]
