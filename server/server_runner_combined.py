import io
import matplotlib
# matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np
import threading
from flask import Flask, request, Response
from PIL import Image
from werkzeug.serving import WSGIRequestHandler

scale = 160/100.0 # Sets the max speed of the car
FORWARD = 80
REVERSE = 80
SIDE_PRIMARY = 60
SIDE_SECONDARY = 0
DIAGONAL_PRIMARY = 90
DIAGONAL_SECONDARY = 65

ku = kl = kr = kd = False
img = None
app = Flask(__name__)


def key_press(event):
	global scale
	# print("pressed: " + str(event.key))
	k = str(event.key)
	if k == "d":
		scale += 0.05
		if scale > 2.55:
			scale = 2.55
	elif k == "a":
		scale -= 0.05
		if scale < 0:
			scale = 0
	else:
		set_flag(k, True)

def key_release(event):
	# print("released: " + str(event.key))
	k = str(event.key)
	if k != "d" and k != "a":
		set_flag(str(event.key), False)

def get_data():
	data = [0, 0]
	if kd:
		data = [-REVERSE, -REVERSE]
	elif ku and not kl and not kr:
		data = [FORWARD, FORWARD]
	elif ku and kl:
		data = [DIAGONAL_SECONDARY, DIAGONAL_PRIMARY]
	elif ku and kr:
		data = [DIAGONAL_PRIMARY, DIAGONAL_SECONDARY]
	elif kl:
		data = [SIDE_SECONDARY, SIDE_PRIMARY]
		# data = [SIDE_PRIMARY, SIDE_SECONDARY]
	elif kr:
		data = [SIDE_PRIMARY, SIDE_SECONDARY]
		# data = [SIDE_SECONDARY, SIDE_PRIMARY]
	
	return [int(-scale*data[0]), int(-scale*data[1])] # Not normal

def get_data_string():
	# Data string looks like: 0255-010
	d = get_data()
	return str(d[0]).zfill(4) + str(d[1]).zfill(4)

def set_flag(key, val):
	global ku, kl, kr, kd
	if key == "up":
		ku = val
	elif key == "left":
		kl = val
	elif key == "right":
		kr = val
	elif key == "down":
		kd = val

def update_graph(num, im): # https://stackoverflow.com/questions/17212722/matplotlib-imshow-how-to-animate
	if img is not None: # Does this need a thread lock?
		# print("animation: graph updated")
		im.set_array(img)

	plt.xlabel("scale: " + str(int(scale*100)) + "     data: " + str(get_data()), fontsize="large")
	return im,

def decode_img(d):
	try:
		img = Image.open(io.BytesIO(d))
		img = np.asarray(img)
		# img = np.flipud(img) # Depends on orientation of the phone in the car
		img = np.fliplr(img)
		return img
	except IOError:
		print('Decoded a bad image: IOError')
	return None


def start_server():
	WSGIRequestHandler.protocol_version = "HTTP/1.1"
	app.run(host='0.0.0.0', debug=False)

@app.route('/', methods=['GET'])
def myget(): # Nothing should be making a GET request
	print("got a GET")
	return "fake get return", 204

@app.route('/', methods=['POST'])
def mypost():
	print("got a POST")
	file = request.files['file']
	if file:
		# print("post has a file, updating img value")
		global img
		img = decode_img(file.read())
	else:
		print("error: post has no file")

	resp = Response(get_data_string())
	# resp.headers['Connection'] = 'Keep-Alive'
	# resp.headers['Keep-Alive'] = 'timeout=5, max=1000'
	return resp, 200


if __name__ == '__main__':
	w = 320
	h = 240
	fig = plt.figure()
	ax = plt.axes(xlim=(0, w), ylim=(0, h))

	plt.xticks([]) # No ticks anywhere
	plt.yticks([])
	fig.canvas.mpl_connect('key_press_event', key_press) # https://matplotlib.org/3.2.1/users/event_handling.html
	fig.canvas.mpl_connect('key_release_event', key_release)
	
	im = plt.imshow(np.random.rand(h, w))

	thread = threading.Thread(target=start_server)
	thread.start()

	# https://matplotlib.org/gallery/animation/basic_example.html
	line_ani = animation.FuncAnimation(fig, update_graph, fargs=(im,), interval=150, blit=False)
	plt.show()
