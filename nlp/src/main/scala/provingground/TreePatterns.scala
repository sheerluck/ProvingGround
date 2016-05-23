package provingground

import edu.stanford.nlp.trees.Tree

import PennTrees._

object TreePatterns {
  object word {
    def unapply(s: String): Option[String] = Some(s.toLowerCase)
  }

  class Pattern[Z](pf: PartialFunction[Tree, Z]) {
    def unapply(t: Tree) = pf.lift(t)
  }

  object IfClause
      extends Pattern({
        case Node("SBAR", List(Node("IN", List(Leaf(word("if")))), t)) => t
      })

  object Then
      extends Pattern({
        case Node(
            "S",
            x :: Node("ADVP", List(Node("RB", List(Leaf(word("then")))))) :: ys) =>
          (x, ys)
      })

  object IfTree
      extends Pattern({
        case Node("S", IfClause(x) :: ys) => (x, ys)
        case IfClause(Then(x, ys)) => (x, ys)
      })

  import Translator._

  val ifPattern = Translator.Pattern[Tree, Functor.IL](IfTree.unapply)

  object Test {
    val ifTrans = ifPattern.join((xl: (Int, List[Int])) =>
          xl._2.headOption map (_ + xl._1))
  }

  object VP extends Pattern({ case Node("VP", xs) => xs })

  object NP extends Pattern({ case Node("NP", xs) => xs })

  object NPVP
      extends Pattern({ case Node("S", List(NP(xs), VP(ys))) => (xs, ys) })

  object SimpleNPVP
      extends Pattern({
        case Node("S", List(NP(List(x)), VP(List(y)))) => (x, y)
      }) {
    val pattern = Translator.Pattern[Tree, Functor.II](unapply)

    def translate[E: ExprLang] = {
      pattern.join(ExprLang.appln[E])
    }
  }

  val npvpPattern = Translator.Pattern[Tree, Functor.LL](NPVP.unapply)
}
