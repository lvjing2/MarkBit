import os, fnmatch

def findReplace(dir, find, replace, filePattern):
	for path, dirs, files in os.walk(os.path.abspath(dir)):
		for filename in fnmatch.filter(files, filePattern):
			filePath = os.path.join(path, filename)
			with open(filePath) as f:
				s = f.read()
			s = s.replace(find, replace)
			with open(filePath, "w") as f:
				f.write(s)

findReplace(".", "org.catrobat.paintroid", "com.liwn.zzl.markbit", "*.java")
findReplace(".", "PaintroidApplication", "MarkBitApplication", "*.java")
