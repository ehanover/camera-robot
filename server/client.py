import requests
import time


url = 'http://192.168.0.225:5000'
# url = 'http://1faaacb0.ngrok.io'

def post(file):
	# file = open('test.jpg', 'rb')
	file_dict = {'file': file}
	r = requests.post(url, files=file_dict)
	# file.close()

def get():
	r = requests.get(url)
	return r.text


if __name__ == '__main__':
	# post()
	# get()

	for i in range(5):

		# with open("test" + str(i) + ".jpg", "rb") as f:
		# 	print("posting... #" + str(i))
		# 	post(f)

		print("getting... #" + str(i))
		print(get())

		time.sleep(1)
