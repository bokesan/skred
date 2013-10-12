all : CombGen
	./CombGen

CombGen : CombGen.hs
	ghc --make CombGen.hs

clean:
	$(RM) CombGen *.o *.hi *.java

