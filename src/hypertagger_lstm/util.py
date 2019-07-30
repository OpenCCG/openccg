import os
import time
import urllib2
import logging
import threading
import datetime
import errno

def maybe_mkdirs(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

def maybe_download(data_dir, source_url, filename):
    if not os.path.exists(data_dir):
        os.mkdir(data_dir)
    filepath = os.path.join(data_dir, filename)
    if os.path.exists(filepath):
        logging.info("Using cached version of {}.".format(filepath))
    else:
        file_url = source_url + filename
        logging.info("Downloading {}...".format(file_url))
        response = urllib2.urlopen(file_url)
        with open(filepath, "wb") as f:
            f.write(response.read())
        statinfo = os.stat(filepath)
        logging.info("Succesfully downloaded {} ({} bytes).".format(file_url, statinfo.st_size))
    return filepath

class Timer:
    def __init__(self, name, active=True):
        self.name = name if active else None

    def __enter__(self):
        self.start = time.time()
        self.last_tick = self.start
        return self

    def __exit__(self, *args):
        if self.name is not None:
            logging.info("{} duration was {}.".format(self.name, self.readable(time.time() - self.start)))

    def readable(self, seconds):
        return str(datetime.timedelta(seconds=int(seconds)))

    def tick(self, message):
        current = time.time()
        logging.info("{} took {} ({} since last tick).".format(message, self.readable(current - self.start), self.readable(current - self.last_tick)))
        self.last_tick = current

class LoggingToFile(object):
    def __init__(self, logdir, filename):
        self.handler = logging.FileHandler(os.path.join(logdir, filename))

    def __enter__(self):
        logging.getLogger().addHandler(self.handler)

    def __exit__(self, *args):
        logging.getLogger().removeHandler(self.handler)
