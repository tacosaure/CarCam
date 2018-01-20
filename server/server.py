#****************** Server ********************
import socket
import sys
import RPi.GPIO as GPIO
import time
import MySQLdb
import os

# Import fichier voiture
import video_dir
import car_dir
import motor

from time import ctime          # Import necessary modules  

# Commande voiture
ctrl_cmd = ['avancer', 'reculer', 'left', 'right', 'stop', 'read cpu_temp', 'stop_turning', 'distance', 'x+', 'x-', 'y+', 'y-', 'xy_home'] 
busnum = 1          # Edit busnum to 0, if you uses Raspberry Pi 1 or 0

HOST = '192.168.43.238' #this is your localhost   dont forget to check the binding
PORT = 8888  # Port d'ecoute

#     Initialise les GPIO de la Raspberry Pi
GPIO.setmode(GPIO.BOARD)
GPIO.setup(7, GPIO.OUT)

# Setup voiture
video_dir.setup(busnum=busnum)
car_dir.setup(busnum=busnum)
motor.setup(busnum=busnum)     # Initialize the Raspberry Pi GPIO connected to the DC motor. 
video_dir.home_x_y()
car_dir.home()

# Definition des fonctions

def onLED():
		GPIO.output(7, True)

def offLED():	
		GPIO.output(7, False)	

def quit(): 
	conn.send("Server has closed\n")
	print("Client has closed")
	
# Creation du socket en definissant son type	

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

#socket.socket: must use to create a socket.
#socket.AF_INET: Address Format, Internet = IP Addresses.
#socket.SOCK_STREAM: two-way, connection-based byte streams.

print 'socket created'
 
#Bind socket to Host and Port

try:
#    s.bind((HOST, PORT))
    s.bind(('', PORT))
except socket.error as err:
    print 'Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1]
    sys.exit()
 
print ('Socket Bind Success!')
 
 
#listen(): This method sets up and start TCP listener.

s.listen(10)
print ('Socket is now listening')

# On accepte la connexion

conn, addr = s.accept() 
print ('Connect with ' + addr[0] + ':' + str(addr[1]))

#On envoie un message au client

try:
    conn.send("Connected\n")
except socket.error as err:
    print("Probleme envoi message") 


try:
	while 1:
		buf = conn.recv(64)	# On recoit un message
		if buf>0:
			buf=buf.decode()	# On decode le message
			print (buf)
			#conn.send("sent\n")	# On envoie un accuse de reception
			#Suivant les String recus on execute des fonctions 
			requete = buf.split(" ") # on decoupe le buf en mot et genere une liste
			if (buf=='quit'):
				quit()
				break
			elif (buf == "startx"):
				sys.exit(1)
			elif (buf == 'onLED'):
				onLED()
			elif (buf == 'offLED'):
				offLED()
				
# Les commandes pour la voiture					
			elif (buf == ctrl_cmd[0]):
				print 'motor moving forward'
				motor.forward()				
			elif (buf == ctrl_cmd[1]):
				print 'recv backward cmd'
				motor.backward()
			elif (buf == ctrl_cmd[2]):
				print 'recv left cmd'
				car_dir.turn_left()
			elif (buf == ctrl_cmd[3]):
				print 'recv right cmd'
				car_dir.turn_right()
			elif (buf == ctrl_cmd[6]):
				print 'recv home cmd'
				car_dir.home()
			elif (buf == ctrl_cmd[4]):
				print 'recv stop cmd'
				motor.ctrl(0)
			elif (buf == ctrl_cmd[5]):
				print 'read cpu temp...'
				#temp = cpu_temp.read()
				#tcpCliSock.send('[%s] %0.2f' % (ctime(), temp))
			elif (buf == ctrl_cmd[8]):
				print 'recv x+ cmd'
				video_dir.move_increase_x()
			elif (buf == ctrl_cmd[9]):
				print 'recv x- cmd'
				video_dir.move_decrease_x()
			elif (buf == ctrl_cmd[10]):
				print 'recv y+ cmd'
				video_dir.move_increase_y()
			elif (buf == ctrl_cmd[11]):
				print 'recv y- cmd'
				video_dir.move_decrease_y()
			elif (buf == ctrl_cmd[12]):
				print 'home_x_y'
				video_dir.home_x_y()	
			elif (buf[0:5] == 'speed'):     # Change the speed
				print buf
				numLen = len(buf) - len('speed')
				if numLen == 1 or numLen == 2 or numLen == 3:
					tmp = buf[-numLen:]
					print 'tmp(str) = %s' % tmp
					spd = int(tmp)
					print 'spd(int) = %d' % spd
					if spd < 24:
						spd = 24
					motor.setSpeed(spd)
					
			elif (buf[0:5] == 'turn='):	#Turning Angle
				print 'buf =', buf
				angle = buf.split('=')[1]
				try:
					angle = int(angle)
					car_dir.turn(angle)
				except:
					print 'Error: angle =', angle
					
			elif (buf[0:8] == 'forward='):
				print 'buf =', buf
				spd = buf[8:]
				try:
					spd = int(spd)
					motor.forward(spd)
				except:
					print 'Error speed =', spd
					
			elif (buf[0:9] == 'backward='):
				print 'buf =', buf
				spd = buf.split('=')[1]
				try:
					spd = int(spd)
					motor.backward(spd)
				except:
					print 'ERROR, speed =', spd	
				
#-------------------------------------------------------------				
				
			elif (requete[0] == "speed"): # on regarde si on veut changer la vitesse
				if(len(requete) == 2):
					if(int(requete[1]) >100):
						requete[1] = "100"
					elif(int(requete[1])<0):
						requete[1] = "0"
					print ("Ajustement de la vitesse a : " + requete[1]) # on change la vitesse : speed(requete[1])
					
			elif ((requete[0] == "check") and (len(requete) == 2)):
				try:
					conn_db = MySQLdb.connect("localhost","root","projet","projet")
					cursor = conn_db.cursor()
				except:
					print("Probleme connexion DB")
					conn.send("Probleme connexion DB\n")
				if (requete[1] == "snapshot"):	
					result_command = os.popen("alpr -c eu /home/pi/motion_pics/lastsnap.jpg","r")
					str_plaque = result_command.read()
					immatriculation = ""
					entete =""
					for entete_ligne in range (0,100):
						if (str_plaque[entete_ligne] == '\n'):
							break
						entete += str_plaque[entete_ligne]
					if (entete != "No license plates found."):
						for ligne in range (25,35):
							if (str_plaque[ligne] == '\t'):
								break
							immatriculation += str_plaque[ligne]
						result_command.close()
						print("plaque : " + immatriculation)
						conn.send("plaque : " + immatriculation + "\n")
						try:
							if(cursor.execute("""SELECT * FROM voiture WHERE immatriculation = %s""", immatriculation)):
								for row in cursor:
									print("True (in database)")
									conn.send("True (in database)\n")
									conn.send(str(row) +"\n")
									break
							else:
								print("False (in database)")	
								conn.send("False (in database)\n")
						except:
							print("Probleme requete")
							conn.send("Probleme requete\n")
					else:
						print("plate recognition has failed")
						conn.send("plate recognition has failed\n")
					
			elif ((requete[0] == "check") and (len(requete) == 3)):
				try:
					conn_db = MySQLdb.connect("localhost","root","projet","projet")
					cursor = conn_db.cursor()
					if (requete[2].islower()):
						requete[2].upper()
				except:
					print("Probleme connexion DB")
					conn.send("Probleme connexion DB\n")
				if (requete[1] == "id"):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE id = %s""", requete[2])):
							for row in cursor:
								print (row)
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row) +"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")	
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
					
				elif (requete[1] == "nom"):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE nom = %s""", requete[2])):
							for row in cursor:
								print row
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row)+"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
					
				elif (requete[1] == "prenom"):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE prenom = %s""", requete[2])):
							for row in cursor:
								print row
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row)+"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
				elif (requete[1] == "statut"):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE statut = %s""", requete[2])):
							for row in cursor:
								print row
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row)+"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
				elif (requete[1] == "immatriculation"):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE immatriculation = %s""", requete[2])):
							for row in cursor:
								print row
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row)+"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
				conn_db.close()
			elif ((requete[0] == "check") and (len(requete) == 5 )):
				try:
					conn_db = MySQLdb.connect("localhost","root","projet","projet")
					cursor = conn_db.cursor()
					if (requete[2].islower()):
						requete[2].upper()
					if (requete[4].islower()):
						requete[4].upper()
				except:
					print("Probleme connexion DB")
					conn.send("Probleme connexion DB\n")
				if ((requete[1] == "nom") and (requete[3] == "prenom")):
					try:
						if(cursor.execute("""SELECT * FROM voiture WHERE nom = %s AND prenom = %s""",(requete[2], requete[4]))):
							for row in cursor:
								print row
								print("True (in database)")
								conn.send("True (in database)\n")
								conn.send(str(row)+"\n")
								break
						else:
							print("False (in database)")
							conn.send("False (in database)\n")
					except:
						print("Probleme requete")
						conn.send("Probleme requete\n")
				conn_db.close()

#Si on fait Ctrl+c, on execute la fonction quit()

except KeyboardInterrupt:
	quit()
except:
	print('Deconnexion du client')
	sys.exit(1)

#On ferme le socket et on vide les GPIO

finally:
	s.close()	#On ferme le socket
	GPIO.cleanup()     #On reset les GPIO          

