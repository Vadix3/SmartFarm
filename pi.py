from os import error
from distutils.util import strtobool
import threading
from typing import SupportsRound
from bson import ObjectId
import spidev
import threading
import os
import time
import Adafruit_DHT
import pymongo
from pymongo import MongoClient

# A command class representing commands sent to the device
# id - the command id
# time - the time command was issued
# device_id - the target device id
# command_type - the command type, for instance 0 = change measurement 
duration
# data - extra data for the command, for example 5 could be the new 
measurement duration


class ReaderThread (threading.Thread):
    def _init_(self, interval, data_collection):
        self.interval = interval  # The measure interval
        super(ReaderThread, self)._init_()
        self._stop_event = threading.Event()
        self.data_collection = data_collection

    def stop(self):
        print("Stop event is set")
        self._stop_event.set()

    def join(self, *args, **kwargs):
        self.stop()
        super(ReaderThread, self).join(*args, **kwargs)

    def run(self):
        print("Running with interval: ", self.interval)
        while not self._stop_event.is_set():
            data = self.read_data()
            self.write_data_to_db(data)
            self._stop_event.wait(self.interval)

    def change_interval(self, interval):
        print("Changing interval to: ", interval)
        self.interval = interval

    def read_data(self):
        print("Reading data with interval: ", self.interval)
        humidity, temperature = Adafruit_DHT.read_retry(
            DHT_SENSOR, DHT_PIN)  # Read humidity and temperature from 
DHT22

        if humidity is not None and temperature is not None:
            print(
                "Temp={0:0.1f}*C  Humidity={1:0.1f}%".format(temperature, 
humidity))
        else:
            print("Failed to retrieve data from humidity sensor")
            humidity = READ_ERROR_VALUE
            temperature = READ_ERROR_VALUE

        # soil moisture
        soil = read_channel(0)
        if (soil != 0):
            print("Soil: "+str(soil))
        else:
            soil = READ_ERROR_VALUE

        # ambient light
        light = read_channel(1)
        if (light != 0):
            print("Light: "+str(light))
        else:
            light = READ_ERROR_VALUE

        # uv light
        uv = read_channel(2)
        if (uv != 0):
            print("UV: "+str(uv))
        else:
            uv = READ_ERROR_VALUE

        # insert
        data = {
            "device": getserial(),
            "date": time.strftime('%d/%m/%y'),
            "time": time.strftime('%H:%M:%S'),
            "humidity": humidity,
            "temperature": temperature,
            "soil": soil,
            "light": light,
            "uv": uv
        }
        return data

    # This method will write the data to the db

    def write_data_to_db(self, data):
        print("Data to write: ", data)
        result = self.data_collection.insert_one(data)
        inserted_id_document = ObjectId(result.inserted_id)
        print("Data inserted with record id:", inserted_id_document)
        # Printing the data inserted
        cursor = data_collection.find()
        for record in cursor:
            outer_id = record['_id']
            if inserted_id_document == outer_id:
                print("Inserted document: ", record)


class Command:
    def _init_(self, id, time, device_id, command_type, data):
        self.id = id
        self.time = time
        self.device_id = device_id
        self.command_type = command_type
        self.data = data

    def _str_(self):
        return " id = " + str(self.id) + "\ntime = " + str(
            self.time) + "\ndevice_id = " + str(self.device_id) + 
"\ncommand_type = " + str(self.command_type) + "\ndata = " + 
str(self.data)


DHT_SENSOR = Adafruit_DHT.DHT22  # sensor type for temperature and 
humidity
DHT_PIN = 4  # Connection pin for the sensor
READ_ERROR_VALUE = -999  # the value that will be given to a measurement 
if it fails
delay = 10  # Delay between measurements (seconds)
reading = False  # A boolean valude to determine if data should be read

spi = spidev.SpiDev()
spi.open(0, 0)
spi.max_speed_hz = 1000000

cluster = MongoClient(
    
"mongodb+srv://Vadix3:Vx121212@cluster0.mcatx.mongodb.net/testdb?retryWrites=true&w=majority")  
# Mongo client
db = cluster["testdb"]  # Selected mongo cluster
commands_collection = db["commands"]  # Selected commands collection
data_collection = db["data"]  # selected data collection
device_collection = db["devices"]
myThread = ReaderThread(delay, data_collection)


def getserial():
    # Extract serial from cpuinfo file
    cpuserial = "0000000000000000"
    try:
        f = open('/proc/cpuinfo', 'r')
        for line in f:
            if line[0:6] == 'Serial':
                cpuserial = line[10:26]
        f.close()
    except:
        cpuserial = "ERROR000000000"

    return cpuserial


# A method that will receive a channel to read from and will return the 
read value from the sensor in that channel
def read_channel(channel):
    val = spi.xfer2([1, (8+channel) << 4, 0])
    data = ((val[1] & 3) << 8) + val[2]
    return data


# This method will parse the given change into a command object and return 
it
def parse_change(change):
    print("parsing document")
    try:
        command_id = change['id']
        time = change['time']
        device_id = change['device_id']
        command_type = change['type']
        data = change['data']
        command = Command(command_id, time, device_id, command_type, data)
        return command
    except error:
        print(error)

# A method to change measure duration


def change_measure_duration(duration):
    sec_time = int(round(float(duration)*60))
    print("Changing duration to: " + str(sec_time)+" seconds")
    global delay
    delay = sec_time
    print("stop")
    stop_collecting_data()
    print("start")
    start_collecting_data()


# A method to start collecting data
def start_collecting_data():
    print("Start collecting data")
    global myThread
    if not myThread.is_alive():
        print("Starting thread")
        myThread = ReaderThread(delay, data_collection)
        myThread.start()
    else:
        print("Thread already running")

# A method to stop collecting data


def stop_collecting_data():
    print("Stop collecting data")
    # if the thread is running, stop it
    global myThread
    if myThread.is_alive():
        print("Joining thread")
        print(threading.get_ident())
        myThread.join()
    else:
        print("Thread is not running")


# This method will listen for changes in the commands collection

def check_command_type(new_command):

    # Change interval command
    if new_command.command_type == 0:
        change_measure_duration(new_command.data)

    # Switch on/off command
    if new_command.command_type == 1:
        bool_value = bool(strtobool(new_command.data))
        if bool_value is True:
            start_collecting_data()
        if bool_value is False:
            stop_collecting_data()

    if new_command.command_type == 2:
        collect_data_once()


# This method will measure data once, now, on a different thread.
# Measure and upload data
def collect_data_once():
    print("Measuring data once")
    write_data_to_db(read_data())
    # Maybe listen for changes again


# This method will watch for changes in the command document
def watch_for_changes():
    print("Listening for changes")
    with commands_collection.watch() as stream:
        for change in stream:
            print(change)
            change = parse_change(change['fullDocument'])
            my_id = getserial()
            print("This device: ", my_id, " Command device: ", 
change.device_id)
            if my_id == change.device_id:
                check_command_type(change)

# This method will check the latest commands issued and


def fetch_device_config():
    print("Checking for latest commands")
    query = {"did": getserial()}
    mydoc = device_collection.find(query)
    is_active = False
    for myDevice in mydoc:
        sec_time = int(round(float(myDevice['measure_interval'])*3600))
        print("Measuring with = ", sec_time)
        global delay
        delay = sec_time
        is_active = myDevice['active']
        print("Device is active = ", is_active)
        break

    if is_active:
        print(threading.get_ident())
        start_collecting_data()
    else:
        print("Device is not on")
    watch_for_changes()


def read_data():
    print("Reading data once")
    humidity, temperature = Adafruit_DHT.read_retry(
        DHT_SENSOR, DHT_PIN)  # Read humidity and temperature from DHT22

    if humidity is not None and temperature is not None:
        print(
            "Temp={0:0.1f}*C  Humidity={1:0.1f}%".format(temperature, 
humidity))
    else:
        print("Failed to retrieve data from humidity sensor")
        humidity = READ_ERROR_VALUE
        temperature = READ_ERROR_VALUE

    # soil moisture
    soil = read_channel(0)
    if (soil != 0):
        print("Soil: "+str(soil))
    else:
        soil = READ_ERROR_VALUE

    # ambient light
    light = read_channel(1)
    if (light != 0):
        print("Light: "+str(light))
    else:
        light = READ_ERROR_VALUE

    # uv light
    uv = read_channel(2)
    if (uv != 0):
        print("UV: "+str(uv))
    else:
        uv = READ_ERROR_VALUE

    # insert
    data = {
        "device": getserial(),
        "date": time.strftime('%d/%m/%y'),
        "time": time.strftime('%H:%M:%S'),
        "humidity": humidity,
        "temperature": temperature,
        "soil": soil,
        "light": light,
        "uv": uv
    }
    return data

# This method will write the data to the db


def write_data_to_db(data):
    print("Data to write: ", data)
    result = data_collection.insert_one(data)
    inserted_id_document = ObjectId(result.inserted_id)
    print("Data inserted with record id:", inserted_id_document)
    # Printing the data inserted
    cursor = data_collection.find()
    for record in cursor:
        outer_id = record['_id']
        if inserted_id_document == outer_id:
            print("Inserted document: ", record)


if _name_ == "_main_":
    print(threading.get_ident())
    fetch_device_config()
