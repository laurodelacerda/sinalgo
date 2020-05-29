import sys
import math

def main():

	# print command line arguments
	n = int(sys.argv[1])
	s = int(math.sqrt(n))

	matrix = dict()
	for x in range(s):
		line = list()
		for y in range(s):
			line.append(s*x + y)

		matrix[x] = line

	print('Nodes: ' + str(n))	
	print('Side: ' + str(s))

	district = dict()
	for i in range(n):			
		
		# add linha
		lin = matrix[int(i/s)]

		# add coluna						
		col = [matrix[l][i%s] for l in range(s)]

		district[i] = lin + sorted(list(set(col) - set(lin)))

	for k, v in district.items():
		print("C" + str(k) + " " + str(v))
		

if __name__ == "__main__":
    main()