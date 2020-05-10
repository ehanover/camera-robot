import numpy as np
import cv2
import io
from PIL import Image
import threading
# import pylab as pl

import matplotlib
# matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

# h = 320
# w = 240 # w*h = 76800
# window = None

# fig = plt.figure()
# im = None
# img_display = None

def decode_img(d):
	try:
		img = Image.open(io.BytesIO(d))
		img = np.asarray(img)
		img = np.flipud(img)
		# img = np.repeat(img[..., 1], 3, axis=1).reshape((w, h, 3))
		# img = np.rot90(img, 3)
		# img = cv2.resize(img, (0, 0), fx=2, fy=2) # TODO find replacement resize function

		return img
	except IndexError:
		print('Bad image: IndexError')
		return
	except IOError:
		print('Bad image: IOError')
		return

def set_img(d):
	global img_display

	img = decode_img(f)
	img_display = img
	# show_img_cv2(img)
	# show_img_pyl(img)
	# show_img_mpl(img)

def show_img_cv2(f):
	cv2.imshow('img', f)
	# cv2.waitKey()
	cv2.waitKey(1000)

def show_img_pyl(f):
	global window
	# window = None
	# im = pl.imread(f)
	if window is None:
		window = pl.imshow(f)
	else:
		window.set_data(f)
	pl.pause(0.1)
	pl.draw()

def show_img_mpl(f):
	global fig, im

	if im is None:
		im = plt.imshow(f)
	else:
		im.set_data(f)
	fig.canvas.draw()

	# plt.show()
	# plt.clf()
	# time.sleep(1)

def animate_init():
	print("animate init...")

def animate(frame):
	print("animating, frame=" + str(frame))
	global im, img_display

	if img_display is None:
		return im,

	if im is None:
		im = plt.imshow(img_display)
	else:
		im.set_data(img_display)

	return im, None



# print("asdf")


# class ImageViewer(threading.Thread):
# 	def __init__(self):
# 		threading.Thread.__init__(self)

# 		fig = plt.figure()
# 		ax = fig.add_subplot(111)
# 		im = None

	
# 	def run(self):
# 		while True:
# 			pass


if __name__ == '__main__':
	FuncAnimation(fig, animate, init_func=animate_init, interval=1000, blit=True)
	plt.show()

	f = open('test0.jpg', 'rb')
	d = f.read()
	f.close()

	set_img(d)
