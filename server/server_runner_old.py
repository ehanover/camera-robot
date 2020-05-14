import threading
import numpy as np
import matplotlib
# matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import server_flask

SCALE = 230/100
FORWARD = int(100*SCALE)
REVERSE = int(70*SCALE)
SIDE_PRIMARY = int(50*SCALE)
SIDE_SECONDARY = int(0*SCALE)
DIAGONAL_PRIMARY = int(80*SCALE)
DIAGONAL_SECONDARY = int(40*SCALE)
ku = kl = kr = kd = False


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

def press(event):
	# print("pressed: " + str(event.key))
	set_flag(str(event.key), True)

def release(event):
	# print("released: " + str(event.key))
	set_flag(str(event.key), False)

def update_line(num, im): # https://stackoverflow.com/questions/17212722/matplotlib-imshow-how-to-animate
	# print("update_line")
	new_data = server_flask.get_img() # TODO does this need a thread lock?
	if new_data is not None:
		# print("animate: updated")
		im.set_array(new_data)

	plt.xlabel("data: " + str(get_data()), fontsize="large")
	return im,

def start_server():
	server_flask.start()


if __name__ == '__main__':
	# https://matplotlib.org/gallery/animation/basic_example.html

	w = 320
	h = 240
	fig = plt.figure()
	ax = plt.axes(xlim=(0, w), ylim=(0, h))

	plt.xticks([]) # No ticks anywhere
	plt.yticks([])

	fig.canvas.mpl_connect('key_press_event', press)
	fig.canvas.mpl_connect('key_release_event', release)
	im = plt.imshow(np.random.rand(h, w))

	# thread = threading.Thread(target=start_server)
	# thread.start()

	line_ani = animation.FuncAnimation(fig, update_line, # init_func=init_line,
		fargs=(im,), interval=550, blit=False)
	plt.show()
