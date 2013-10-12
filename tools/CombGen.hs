-- generate code for combinators


import List (intersperse)
import Char (toUpper)
import IO

type Var = Char

data Expr = EVar Var
          | EApp Expr Expr

isVar, isApp :: Expr -> Bool
isVar (EVar _) = True
isVar _ = False

isApp (EApp _ _) = True
isApp _ = False

eVar :: Expr -> Var
eVar (EVar x) = x

instance Show Expr where
  showsPrec _ e = se False e
    where
      se _ (EVar x) = showChar x
      se False (EApp f x) = se False f . showChar ' ' . se True x
      se True  (EApp f x) = showChar '(' . se False f . showChar ' ' . se True x . showChar ')'


type Instr = String


-- projection combinators
combGen :: (String, [Var], Expr) -> Bool -> CodeGen ()
combGen (c, args, expr@(EVar v)) True =
   do bindArgs args expr
      emitSet (arity - 1) ("a_" ++ show expr)
      emitPop (arity - 1)
      emit "c.get1().overwriteHole()"
      emit "c.eval()"
      emit "Node result = c.getTos()"
      emit "c.get1().overwriteInd(result)"
      emitSet 1 "result"
      emitPop 1
      emitUnwind
  where
    arity = length args
combGen (c, args, expr@(EVar v)) False =
   do bindArgs args expr
      emitPop arity
      emit ("c.getTos().overwriteInd(" ++ result ++ ")")
      emit ("c.setTos(" ++ result ++ ")")
      emitUnwind
  where
    arity = length args
    result = "a_" ++ show expr

-- combinators resulting in an application
combGen (c, args, expr@(EApp f a)) _ =
   do bindArgs args expr
      emit ("Node redex = " ++ getSpine arity)
      f' <- buildNode f
      a' <- buildNode a
      emit ("redex.overwriteApp(" ++ f' ++ ", " ++ a' ++ ")")
      emitSet (arity - 1) f'
      emitPop (arity - 1)
      emitUnwind
  where
    arity = length args

buildNode :: Expr -> CodeGen String
buildNode (EVar x) = return ("a_" ++ [x])
buildNode (EApp f a) = do f' <- buildNode f
                          a' <- buildNode a
                          r  <- newVar "g"
                          emit ("Node " ++ r ++ " = c.mkApp(" ++ f' ++ ", " ++ a' ++ ")")
                          return r



bindArgs :: [Var] -> Expr -> CodeGen ()
bindArgs args expr = loop args 1
  where loop [] _ = return ()
        loop (v:vs) n = do bindArg v n; loop vs (n+1)
  
        bindArg v n | occurs v expr = emit ("Node a_" ++ [v] ++ " = " ++ getArg n)
                    | otherwise = return ()



-- variable occurs in expression?
occurs :: Var -> Expr -> Bool
occurs v (EVar x) = v == x
occurs v (EApp f a) = occurs v f || occurs v a



combDefns = [
  ("S", "fgx", EApp (EApp (EVar 'f') (EVar 'x')) (EApp (EVar 'g') (EVar 'x'))),
  ("K", "cx", EVar 'c'),
  ("K'", "xc", EVar 'c'),
  ("I", "x", EVar 'x'),
  ("J", "fgx", EApp (EVar 'f') (EVar 'g')),
  ("J'", "kfgx", EApp (EApp (EVar 'k') (EVar 'f')) (EVar 'g')),
  ("C", "fxy", EApp (EApp (EVar 'f') (EVar 'y')) (EVar 'x')),
  ("B", "fgx", EApp (EVar 'f') (EApp (EVar 'g') (EVar 'x'))),
  ("B*", "cfgx", EApp (EVar 'c') (EApp (EVar 'f') (EApp (EVar 'g') (EVar 'x')))),
  ("B'", "cfgx", EApp (EApp (EVar 'c') (EVar 'f')) (EApp (EVar 'g') (EVar 'x'))),
  ("C'", "cfgx", EApp (EApp (EVar 'c') (EApp (EVar 'f') (EVar 'x'))) (EVar 'g')),
  -- S' c f g x = c (f x) (g x)
  ("S'", "cfgx", EApp (EApp (EVar 'c') (EApp (EVar 'f') (EVar 'x'))) (EApp (EVar 'g') (EVar 'x'))),
  ("W", "fx", EApp (EApp (EVar 'f') (EVar 'x')) (EVar 'x'))
 ]

intOps = [("add", "+"), ("sub", "-"), ("mul", "*"),
          ("div", "/"), ("rem", "%"),
          ("Rsub", "-"), ("Rdiv", "/"), ("Rrem", "%")]

relOps = [("less", "PrimLess", "<"),
          ("greater", "PrimGreater", ">"),
          ("less_eq", "PrimLessEq", "<="),
          ("gr_eq", "PrimGreaterEq", ">="),
          ("eq", "PrimEq", "=="),
          ("neq", "PrimNeq", "!=")]

main = do mapM_ showDef combDefns
          mapM_ (\(name, op) -> genBinOpInt name op (isRevOp name)) intOps
          mapM_ (\(name, cname, op) -> genRelOp name cname op) relOps
 where
   isRevOp ('R':name) = True
   isRevOp _ = False
   showDef (c,a,e) = do putStr c
                        putStr " "
                        putStr (intersperse ' ' a)
                        putStr " = "
                        putStrLn (show e)
                        genCombs (c,a,e)

genCombs :: (String, [Var], Expr) -> IO ()
genCombs s@(c, a, e@(EVar _)) = do genComb s False
				   genComb s True
genCombs s = genComb s False

putCode :: Handle -> [Instr] -> IO ()
putCode h = mapM_ (\i -> if null i then return () else do hPutStr h "        "; hPutStr h i; hPutStrLn h ";")

genComb :: (String, [Var], Expr) -> Bool -> IO ()
genComb (c,a,e) eval =
                   do h <- openFile fileName WriteMode
                      hPutStrLn h "package de.bokeh.skred.red;\n"
                      hPutStrLn h "/**"
                      hPutStrLn h (" * The " ++ c ++ " combinator.")
                      hPutStrLn h " * <p>"
                      hPutStrLn h " * Reduction rule:"
                      hPutStrLn h (" * " ++ c ++ " " ++ intersperse ' ' a ++ " ==&gt; " ++ show e)
                      hPutStrLn h " */"
                      hPutStrLn h ("class " ++ className ++ " extends Function {\n")
                      hPutStrLn h ("    public " ++ className ++ "() {")
                      hPutStrLn h ("        super(\"" ++ c ++ "\", " ++ show arity ++ ");")
                      hPutStrLn h "    }\n"
                      hPutStrLn h "    @Override"
                      hPutStrLn h "    Node exec(RedContext c) {"
                      let code = runCG (combGen (c,a,e) eval)
                      putCode h code
                      hPutStrLn h "    }\n"
                      hPutStrLn h "}"
                      hClose h
  where
    arity = length a
    className = "Comb" ++ map toJava c ++ if eval then "_Eval" else ""
    fileName = className ++ ".java"

toJava :: Char -> Char
toJava '\'' = '1'
toJava '*' = 's'
toJava c = c
                      
initCap :: String -> String
initCap "" = ""
initCap (c:cs) = Char.toUpper c : cs


genBinOpInt :: String -> String -> Bool -> IO ()
genBinOpInt name op rev =
                   do h <- openFile fileName WriteMode
                      hPutStrLn h "package de.bokeh.skred.red;\n"
                      hPutStrLn h "/**"
                      hPutStrLn h (" * Int " ++ name)
                      hPutStrLn h " */"
                      hPutStrLn h ("class " ++ className ++ " extends Function {\n")
                      hPutStrLn h ("    public " ++ className ++ "() {")
                      hPutStrLn h ("        super(\"" ++ name ++ "\", 2);")
                      hPutStrLn h "    }\n\n\
                                  \    @Override\n\
                                  \    Node exec(RedContext c) {\n\
                                  \        c.rearrange2();\n\
                                  \        c.eval();\n\
                                  \        c.swap();\n\
                                  \        c.eval();\n\
                                  \        Node a2 = c.getTos();\n\
                                  \        Node a1 = c.get1();\n\
                                  \        int n1 = a1.intValue();\n\
                                  \        int n2 = a2.intValue();"
                      hPutStrLn h (if rev then "        int r = n2 " ++ op ++ " n1;"
                                          else "        int r = n1 " ++ op ++ " n2;")
                      hPutStrLn h "        Node result = Int.valueOf(r);\n\
                                  \        c.pop2();\n\
                                  \        c.getTos().overwriteInd(result);\n\
                                  \        c.setTos(result);\n\
                                  \        return result;\n\
                                  \    }\n\n\
                                  \}"
                      hClose h
  where
    className = "Prim" ++ initCap name ++ "Int"
    fileName = className ++ ".java"


genRelOp :: String -> String -> String -> IO ()
genRelOp name className op =
                   do h <- openFile fileName WriteMode
                      hPutStrLn h "package de.bokeh.skred.red;\n"
                      hPutStrLn h "/**"
                      hPutStrLn h (" * Relop " ++ name)
                      hPutStrLn h " */"
                      hPutStrLn h ("class " ++ className ++ " extends Function {\n")
                      hPutStrLn h ("    public " ++ className ++ "() {")
                      hPutStrLn h ("        super(\"" ++ name ++ "\", 2);")
                      hPutStrLn h "    }\n\n\
                                  \    @Override\n\
                                  \    Node exec(RedContext c) {\n\
                                  \        c.rearrange2();\n\
                                  \        c.eval();\n\
                                  \        c.swap();\n\
                                  \        c.eval();\n\
                                  \        Node a2 = c.getTos();\n\
                                  \        Node a1 = c.get1();\n\
                                  \        int n1 = a1.intValue();\n\
                                  \        int n2 = a2.intValue();"
                      hPutStrLn h ("        boolean r = n1 " ++ op ++ " n2;")
                      hPutStrLn h "        Node result = Data.valueOf(r?1:0);\n\
                                  \        c.pop2();\n\
                                  \        c.getTos().overwriteInd(result);\n\
                                  \        c.setTos(result);\n\
                                  \        return result;\n\
                                  \    }\n\n\
                                  \}"
                      hClose h
  where
    fileName = className ++ ".java"




data CGState = CG Int [Instr]

newtype CodeGen a = CGM (CGState -> (CGState, a))


runCG :: CodeGen a -> [Instr]
runCG (CGM m) = let (CG _ is, _) = m (CG 1 []) in reverse is


instance Monad CodeGen where
  return x = CGM (\st -> (st, x))
  (CGM m) >>= f = CGM (\s -> let (s',x) = m s; CGM f' = f x in f' s')

emit :: Instr -> CodeGen ()
emit i = CGM (\(CG ns is) -> (CG ns (i:is), ()))

newVar :: String -> CodeGen String
newVar prefix = CGM (\(CG ns is) -> (CG (ns+1) is, prefix ++ show ns))

getCode :: CodeGen [Instr]
getCode = CGM (\st@(CG _ is) -> (st, reverse is))

mAX_SPECIALIZED_OFFSET :: Int
mAX_SPECIALIZED_OFFSET = 3

-- instruction helpers
emitPop :: Int -> CodeGen ()
emitPop 0 = return ()
emitPop n | n <= mAX_SPECIALIZED_OFFSET = emit ("c.pop" ++ shows n "()")
          | otherwise                   = emit ("c.pop(" ++ shows n ")")

emitSet :: Int -> String -> CodeGen ()
emitSet 0 x = emit ("c.setTos(" ++ x ++ ")")
emitSet n x | n <= mAX_SPECIALIZED_OFFSET = emit ("c.set" ++ show n ++ "(" ++ x ++ ")")
            | otherwise                   = emit ("c.set(" ++ show n ++ ", " ++ x ++ ")")

emitUnwind :: CodeGen ()
emitUnwind = emit "return null"

getSpine, getArg :: Int -> String

getSpine 0 = "c.getTos()"
getSpine n | n <= mAX_SPECIALIZED_OFFSET = "c.get" ++ show n ++ "()"
           | otherwise                   = "c.get(" ++ show n ++ ")"

getArg n | n <= mAX_SPECIALIZED_OFFSET = "c.getArg" ++ show n ++ "()"
         | otherwise                   = "c.getArg(" ++ show n ++ ")"
