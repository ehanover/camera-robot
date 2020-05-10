import requests
import time


def post(file):
	# url = 'http://192.168.0.225:5000'
	url = 'http://1faaacb0.ngrok.io'

	# file = open('test.jpg', 'rb')
	file_dict = {'file': file}
	r = requests.post(url, files=file_dict)
	# file.close()

def get():
	url = 'http://192.168.0.225:5000'
	r = requests.get(url)
	print(r.text)


if __name__ == '__main__':
	# post()
	# get()

	for i in range(3):
		with open("test" + str(i) + ".jpg", "rb") as f:
			print("posting... #" + str(i))
			post(f)
		time.sleep(1.1)
