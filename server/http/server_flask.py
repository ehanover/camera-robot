from flask import Flask, request

from PIL import Image
import io
import numpy as np
# import server_img
# import cv2
# import threading
# import time

app = Flask(__name__)
img = None

# @app.before_first_request
# def mybefore():
# 	# print("before_first_request")

# 	def run_job():
# 		fig = plt.figure()

# 		def update_line(num, data, line):
# 			line.set_data(data[..., :num])
# 			return line,

# 		data = np.random.rand(2, 25)
# 		l, = plt.plot([], [], 'r-')
# 		plt.xlim(0, 1)
# 		plt.ylim(0, 1)
# 		plt.xlabel('x')
# 		plt.title('test')
# 		# plt.show()

# 		line_ani = animation.FuncAnimation(fig, update_line, 25, 
# 			fargs=(data, l), interval=50, blit=True)

# 		# while True:
# 		# 	print("Run recurring task")
# 		# 	l.set_ydata(np.random.rand(10))
# 		# 	time.sleep(2)

# 	# thread = threading.Thread(target=run_job)
# 	# thread.start()
# 	run_job()

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

def get_data():
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
		print("post has a file, displaying")

		# server_img.set_img(file.read())
		global img
		img = decode_img(file.read())
		# cv2.imshow('img', img)
		# cv2.waitKey(0)
		# show_img_mpl(img)
	else:
		print("post has no file")

		# b = io.BytesIO
	return "fake post return", 200


# if __name__ == '__main__':
	# app.run(host='0.0.0.0')

def start():
	app.run(host='0.0.0.0')