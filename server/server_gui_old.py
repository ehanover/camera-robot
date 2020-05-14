import pygame

SCALE = 230/100

FORWARD = int(100*SCALE)
REVERSE = int(70*SCALE)

SIDE_PRIMARY = int(50*SCALE)
SIDE_SECONDARY = int(0*SCALE)

DIAGONAL_PRIMARY = int(80*SCALE)
DIAGONAL_SECONDARY = int(40*SCALE)

running = True

ku = False
kl = False
kr = False
kd = False

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

def start():
	global ku, kl, kr, kd
	pygame.init()
	# pygame.font.init()
	screen = pygame.display.set_mode((400, 200))
	clock = pygame.time.Clock()
	myfont = pygame.font.SysFont('FreeSans', 40)

	while running:
		clock.tick(20) # FPS value

		for event in pygame.event.get():
			if event.type == pygame.QUIT:
				quit()
				return
			elif event.type == pygame.KEYDOWN:
				if event.key == pygame.K_UP:
					# data = [FORWARD, FORWARD]
					ku = True
				elif event.key == pygame.K_LEFT:
					# data = [SIDE_SECONDARY, SIDE_PRIMARY]
					kl = True
				elif event.key == pygame.K_RIGHT:
					# data = [SIDE_PRIMARY, SIDE_SECONDARY]
					kr = True
				elif event.key == pygame.K_DOWN:
					# data = [-REVERSE, -REVERSE]
					kd = True
			elif event.type == pygame.KEYUP:
				if event.key == pygame.K_UP:
					ku = False
				elif event.key == pygame.K_LEFT:
					kl = False
				elif event.key == pygame.K_RIGHT:
					kr = False
				elif event.key == pygame.K_DOWN:
					kd = False

		screen.fill((255, 255, 255))
		# screen.blit(image, rect)

		data = get_data()
		f1 = myfont.render("L: " + str(data[0]), False, (0, 0, 0))
		f2 = myfont.render("R: " + str(data[1]), False, (0, 0, 0))
		screen.blit(f1, (30, 60))
		screen.blit(f2, (230, 60))

		pygame.display.update()

def quit():
	global running, ku, kl, kr, kd
	running = False
	ku = kl = kr = kd = False


if __name__ == '__main__':
	start()