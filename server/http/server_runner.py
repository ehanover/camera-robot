import threading
import numpy as np
import matplotlib
# # matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import server_flask
import atexit


def start_server():
	server_flask.start()

def update_line(num, im): # https://stackoverflow.com/questions/17212722/matplotlib-imshow-how-to-animate
	# print("update_line")
	# im.set_array(im.get_array() - 0.1)

	new_data = server_flask.get_data() # TODO does this need a thread lock?
	if new_data is not None:
		# print("animate: updated")
		im.set_array(new_data)
	else:
		# print("animate: skip")
		pass

	return im,

if __name__ == '__main__':
	fig = plt.figure()

	# data = np.random.rand(2, 25)
	# l, = plt.plot([], [], 'r-')
	w = 320
	h = 240
	ax = plt.axes(xlim=(0, w), ylim=(0, h))
	plt.xticks([])
	plt.yticks([])
	# plt.xlim(0, 1)
	# plt.ylim(0, 1)
	# plt.xlabel('x')
	# plt.title('test')
	# plt.show()
	im = plt.imshow(np.random.rand(h, w))

	# start_server()
	thread = threading.Thread(target=start_server)
	thread.start()

	line_ani = animation.FuncAnimation(fig, update_line, # init_func=init_line,
		fargs=(im,), interval=600, blit=True)
	plt.show()


	# while True:
	# 	print("Run recurring task")
	# 	l.set_ydata(np.random.rand(10))
	# 	time.sleep(2)




