from flask import Flask, request

from PIL import Image
import io
import numpy as np
import threading
# import server_gui
# import time

app = Flask(__name__)
img = None


def decode_img(d):
	try:
		img = Image.open(io.BytesIO(d))
		img = np.asarray(img)
		img = np.flipud(img)
		# img = np.repeat(img[..., 1], 3, axis=1).reshape((w, h, 3))
		# img = np.rot90(img, 3)
		# img = cv2.resize(img, (0, 0), fx=2, fy=2) # TODO find replacement resize function

		return img
	# except IndexError:
	# 	print('Bad image: IndexError')
	# 	return
	except IOError:
		print('Bad image: IOError')
		return

def get_img():
	# TODO return None if img hasn't changed?
	return img

@app.route('/', methods=['GET'])
def myget():
	print("got a GET")
	return "fake get return", 200

@app.route('/', methods=['POST'])
def mypost():
	print("got a POST")
	file = request.files['file']
	if file:
		print("post has a file, updating img value")
		global img
		img = decode_img(file.read())
	else:
		print("post has no file")

	df = "0,0"
	try:
		d = server_gui.get_data()
		df = str(d[0]) + "," + str(d[1])
	except Exception as e:
		print("Error in getting pygame data: " + str(e))

	return df, 200

# def start_gui():
# 	server_gui.start()

def start():
	# thread = threading.Thread(target=server_gui.start())
	# thread = threading.Thread(target=start_gui())
	# thread.start()
	
	app.run(host='0.0.0.0')