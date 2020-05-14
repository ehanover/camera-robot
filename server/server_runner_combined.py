import io
import matplotlib
# matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np
import threading
from flask import Flask, request
from PIL import Image

SCALE = 230/100 # Effectively sets the max speed of the car
FORWARD = int(100*SCALE)
REVERSE = int(70*SCALE)
SIDE_PRIMARY = int(50*SCALE)
SIDE_SECONDARY = int(0*SCALE)
DIAGONAL_PRIMARY = int(80*SCALE)
DIAGONAL_SECONDARY = int(40*SCALE)

ku = kl = kr = kd = False
img = None
app = Flask(__name__)


def key_press(event):
	# print("pressed: " + str(event.key))
	set_flag(str(event.key), True)

def key_release(event):
	# print("released: " + str(event.key))
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
	elif kr:
		data = [SIDE_PRIMARY, SIDE_SECONDARY]
	
	return data

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
	# print("update_line")
	if img is not None: # TODO does this need a thread lock?
		# print("animation: graph updated")
		im.set_array(img)

	plt.xlabel("data: " + str(get_data()), fontsize="large")
	return im,

def decode_img(d):
	try:
		img = Image.open(io.BytesIO(d))
		img = np.asarray(img)
		img = np.flipud(img)
		return img
	except IOError:
		print('Decoding bad image: IOError')
	return None



def start_server():
	app.run(host='0.0.0.0')

@app.route('/', methods=['GET'])
def myget(): # There shouldn't be any get requests
	print("got a GET")
	return "fake get return", 204

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
	try: # TODO probably don't need this try/catch
		d = get_data()
		df = str(d[0]) + "," + str(d[1])
	except Exception as e:
		print("Error in getting keyboard data: " + str(e))

	return df, 200



if __name__ == '__main__':
	w = 320
	h = 240
	fig = plt.figure()
	ax = plt.axes(xlim=(0, w), ylim=(0, h))

	plt.xticks([]) # No ticks anywhere
	plt.yticks([])
	# https://matplotlib.org/3.2.1/users/event_handling.html
	fig.canvas.mpl_connect('key_press_event', key_press)
	fig.canvas.mpl_connect('key_release_event', key_release)
	
	im = plt.imshow(np.random.rand(h, w))

	thread = threading.Thread(target=start_server)
	thread.start()

	# https://matplotlib.org/gallery/animation/basic_example.html
	line_ani = animation.FuncAnimation(fig, update_graph,
		fargs=(im,), interval=400, blit=False)
	plt.show()
