package provingground

import HoTT._

import UnicodeSyms.UnivSym

object TermFormat {}

object PrintFormat extends TermRec[String] {
  val syms = UnicodeSyms

  val specialTerms: PartialFunction[Term, String] = Map()

  def fromString(str: String)(implicit typ: Typ[Term]): String = str

  def appln(func: String, arg: String): String = func + "(" + arg + ")"

  def arrow(dom: String, codom: String): String =
    dom + " " + syms.Arrow + " " + codom

  def lambda(variable: String, typ: String, value: String): String =
    s"($variable : $typ) ${syms.MapsTo}  $value"

  def equality(dom: String, lhs: String, rhs: String) =
    s"$lhs = $rhs (in $dom)"

  def pi(fibre: String): String = s"${syms.Pi}($fibre)"

  def sigma(fibre: String): String = s"${syms.Sigma}($fibre)"

  def plus(first: String, scnd: String): String = first + " + " + scnd

  def pair(first: String, second: String) =
    "(" + first + " , " + second + ")"

  def symbobj(term: SymbObj[Term]): String =
    term.name.toString

  def symbtyp(typ: SymbTyp): String = typ.name.toString

  def symbolic(name: AnySym, typ: Typ[Term]): String =
    name.toString

  def univ(n: Int) = s"${UnivSym}"
}

object LatexFormat extends TermRec[String] {
  def latex(t: Term) = (apply(t).replace("$", "."))

  val specialTerms: PartialFunction[Term, String] = Map()

  def fromString(str: String)(implicit typ: Typ[Term]): String = str

  def appln(func: String, arg: String): String = func + "(" + arg + ")"

  def arrow(dom: String, codom: String): String =
    dom + " \to " + codom

  def lambda(variable: String, typ: String, value: String): String =
    s"($variable : $typ)" + " \\mapsto " + s"$value"

  def equality(dom: String, lhs: String, rhs: String) =
    s"$lhs = $rhs (in $dom)"

  def pi(fibre: String): String = s"\prod\limits_{$fibre}"

  def sigma(fibre: String): String = s"\sum\limits_{$fibre}"

  def plus(first: String, scnd: String): String = first + " + " + scnd

  def pair(first: String, second: String) =
    "(" + first + " , " + second + ")"

  def symbobj(term: SymbObj[Term]): String =
    term.name.toString

  def symbtyp(typ: SymbTyp): String = typ.name.toString

  def symbolic(name: AnySym, typ: Typ[Term]): String =
    name.toString

  def univ(n: Int) = "\\mathcal{U}"
}

object FansiFormat extends TermRec[fansi.Str] {
  import fansi.Str
  import fansi.Color._

  val syms = UnicodeSyms

  val specialTerms: PartialFunction[Term, Str] = Map()

  def fromString(str: String)(implicit typ: Typ[Term]): Str = Str(str)

  def appln(func: Str, arg: Str): Str = func ++ "(" ++ arg ++ ")"

  def arrow(dom: Str, codom: Str): Str =
    dom ++ " " ++ LightRed(syms.Arrow) ++ " " ++ codom

  def lambda(variable: Str, typ: Str, value: Str): Str =
    Str("(") ++ variable ++ Yellow(" : ") ++ typ ++ ") " ++ LightRed(
      syms.MapsTo) ++ " " ++ value

  def equality(dom: Str, lhs: Str, rhs: Str) =
    lhs ++ LightRed(" = ") ++ rhs ++ " (in " ++ dom ++ ")"

  def pi(fibre: Str): Str =
    Cyan(Str(syms.Pi)) ++ LightYellow("(") ++ fibre ++ LightYellow(")")

  def sigma(fibre: Str): Str =
    Cyan(Str(syms.Sigma)) ++ LightYellow("(") ++ fibre ++ LightYellow(")")

  def plus(first: Str, scnd: Str): Str = first ++ LightRed(Str(" + ")) ++ scnd

  def pair(first: Str, second: Str) =
    Str("(") ++ first ++ " , " ++ second ++ ")"

  def symbobj(term: SymbObj[Term]): Str =
    Green(Str(term.name.toString))

  def symbtyp(typ: SymbTyp): Str = Cyan(Str(typ.name.toString))

  def symbolic(name: AnySym, typ: Typ[Term]): Str =
    Magenta(Str(name.toString))

  def univ(n: Int) = LightCyan(Str(UnivSym))
}

import fansi._

import fansi.Color._

object FansiTranslate {
  import TermPatterns._

  val syms = UnicodeSyms

  def apply(x: Term) =
    fansiTrans(x) map (_.toString()) getOrElse (x.toString())

  // import fansi.Color.LightRed

  val fansiTrans =
    Translator.Empty[Term, Str] || formalAppln >>> {
      case (func, arg) => func ++ "(" ++ arg ++ ")"
    } || funcTyp >>> {
      case (dom, codom) =>
        Str("(") ++ dom ++ " " ++ Color.LightRed(syms.Arrow) ++ " " ++ codom ++ ")"
    } || lambdaTriple >>> {
      case ((variable, typ), value) =>
        (Str("(") ++ variable ++ Yellow(" : ") ++ typ ++ ") " ++ LightRed(
          syms.MapsTo) ++ " " ++ value)
    } || piTriple >>> {
      case ((variable, typ), value) =>
        (Cyan(Str(syms.Pi)) ++ LightYellow("(") ++ variable ++ Yellow(" : ") ++ typ ++ LightYellow(
          ")") ++ LightRed("{ ") ++ value ++ LightRed(" }"))
    } || sigmaTriple >>> {
      case ((variable, typ), value) =>
        (Cyan(Str(syms.Sigma)) ++ LightYellow("(") ++ variable ++ Yellow(" : ") ++ LightRed(
          "{ ") ++ value ++ LightRed(" }"))
    } || universe >>> { (n) =>
      (LightCyan(Str(UnivSym)))
    } || symName >>> ((s) => (Str(s))) ||
      prodTyp >>> { case (first, second) => (first ++ syms.Prod ++ second) } ||
      absPair >>> {
      case (first, second) => (Str("(") ++ first ++ " , " ++ second ++ ")")
    }  ||
      // identityTyp >>> {case ((dom, lhs), rhs) => (lhs ++ LightRed(" = ") ++ rhs ++ " (in " ++ dom ++ ")")} || // invoke if we want expanded equality
      equation >>> { case (lhs, rhs) => (lhs ++ LightRed(" = ")) ++ rhs } ||
      plusTyp >>> {
        case (first, scnd) => (first ++ LightRed(Str(" + ")) ++ scnd)
      } ||
      indRecFunc >>> {
        case (index, (dom, (codom, defnData))) =>
          defnData.foldLeft(s"rec($dom${index.mkString("(", ")(", ")")})($codom)"){
            case (head, d) => s"$head($d)"
          }
        } ||
      recFunc >>> {
        case (dom, (codom, defnData)) =>
          defnData.foldLeft(s"rec($dom)($codom)"){
            case (head, d) => s"$head($d)"
          }
      } ||
      indInducFunc >>> {
        case (index, (dom, (depcodom, defnData))) =>
          val h = Str(s"induc($dom${index.mkString("(", ")(", ")")})($depcodom)")
          defnData.foldLeft(h){
            case (head, d) => s"$head($d)"
          }
      }  ||
      inducFunc >>> {
        case (dom, (depcodom, defnData)) =>
          val h = Str(s"induc($dom)($depcodom)")
          defnData.foldLeft(h){
            case (head, d) => s"$head($d)"
          }
      }

  import pprint._
  implicit val termprint: PPrinter[Term] = new PPrinter[Term] {
    def render0(t: Term, c: Config) = List(apply(t)).toIterator
  }
}

object TeXTranslate{
  import TermPatterns._

  val syms = UnicodeSyms

  def apply(x: Term) =
    texTrans(x) map (_.toString()) getOrElse (x.toString())

  // import fansi.Color.LightRed

  val texTrans =
    Translator.Empty[Term, String] || formalAppln >>> {
      case (func, arg) => func ++ "(" ++ arg ++ ")"
    } || funcTyp >>> {
      case (dom, codom) =>
       s"""$dom \\to $codom"""
    } || lambdaTriple >>> {
      case ((variable, typ), value) =>
      s"""($variable : $typ) \\mapsto $value"""
    } || piTriple >>> {
      case ((variable, typ), value) =>
        s"""\\prod\\limits_{$variable : $typ} $value"""
    } || sigmaTriple >>> {
      case ((variable, typ), value) =>
        s"""\\sum\\limits_{$variable : $typ} $value"""
    } || universe >>> { (n) =>
      s"""\\mathcal{U}_$n"""
    } || symName >>> ((s) => s) ||
      prodTyp >>>
      { case (first, second) => s"""$first \\times $second""" } ||
    absPair >>> {
      case (first, second) => s"""($first, $second)"""
    } ||
      // identityTyp >>> {case ((dom, lhs), rhs) => (lhs ++ LightRed(" = ") ++ rhs ++ " (in " ++ dom ++ ")")} || // invoke if we want expanded equality
      equation >>> { case (lhs, rhs) => s"$lhs = $rhs" } ||
      plusTyp >>> {
        case (first, scnd) => s"""$first \\oplus $scnd"""
      } ||
      indRecFunc >>> {
        case (index, (dom, (codom, defnData))) =>
          defnData.foldLeft(s"rec($dom${index.mkString("(", ")(", ")")})($codom)"){
            case (head, d) => s"$head($d)"
          }
        } ||
      recFunc >>> {
        case (dom, (codom, defnData)) =>
          defnData.foldLeft(s"rec($dom)($codom)"){
            case (head, d) => s"$head($d)"
          }
      } ||
      indInducFunc >>> {
        case (index, (dom, (depcodom, defnData))) =>
          val h = s"induc($dom${index.mkString("(", ")(", ")")})($depcodom)"
          defnData.foldLeft(h){
            case (head, d) => s"$head($d)"
          }
      }  ||
      inducFunc >>> {
        case (dom, (depcodom, defnData)) =>
          val h = s"induc($dom)($depcodom)"
          defnData.foldLeft(h){
            case (head, d) => s"$head($d)"
          }
      }

}

trait FansiShow[-U] {
  def show(x: U): String
}

object FansiShow {
  implicit class View[U: FansiShow](x: U) {
    def fansi = implicitly[FansiShow[U]].show(x)
  }

  implicit def term[U <: Term]: FansiShow[U] = new FansiShow[U] {
    def show(x: U) = FansiTranslate(x)
    // FansiFormat(x).toString
  }

  implicit def list[U: FansiShow]: FansiShow[List[U]] =
    new FansiShow[List[U]] {
      def show(x: List[U]) = (x map (_.fansi)).mkString("[", "\n", "]")
    }

  implicit def vec[U: FansiShow]: FansiShow[Vector[U]] =
    new FansiShow[Vector[U]] {
      def show(x: Vector[U]) = (x map (_.fansi)).mkString("[", "\n", "]")
    }

  implicit def set[U: FansiShow]: FansiShow[Set[U]] = new FansiShow[Set[U]] {
    def show(x: Set[U]) = (x map (_.fansi)).toString
  }

  implicit def tuple2[U1: FansiShow, U2: FansiShow]: FansiShow[(U1, U2)] =
    new FansiShow[(U1, U2)] {
      def show(tup: (U1, U2)): String = (tup._1.fansi, tup._2.fansi).toString
    }

  implicit def tuple3[U1: FansiShow, U2: FansiShow, U3: FansiShow]
    : FansiShow[(U1, U2, U3)] =
    new FansiShow[(U1, U2, U3)] {
      def show(tup: (U1, U2, U3)): String =
        (tup._1.fansi, tup._2.fansi, tup._3.fansi).toString
    }

  implicit def tuple4[U1: FansiShow,
                      U2: FansiShow,
                      U3: FansiShow,
                      U4: FansiShow]: FansiShow[(U1, U2, U3, U4)] =
    new FansiShow[(U1, U2, U3, U4)] {
      def show(tup: (U1, U2, U3, U4)): String =
        (tup._1.fansi, tup._2.fansi, tup._3.fansi, tup._4.fansi).toString
    }

  implicit def string: FansiShow[String] = new FansiShow[String] {
    def show(x: String) = x
  }

  implicit def num[U: Numeric]: FansiShow[U] = new FansiShow[U] {
    def show(x: U) = x.toString
  }

  implicit def mapp[U: FansiShow, V: FansiShow]: FansiShow[Map[U, V]] =
    new FansiShow[Map[U, V]] {
      def show(m: Map[U, V]) =
        (for ((x, y) <- m) yield (x.fansi, y.fansi)).toMap.toString
    }

  implicit def weighted[U: FansiShow]: FansiShow[Weighted[U]] =
    new FansiShow[Weighted[U]] {
      def show(x: Weighted[U]) =
        s"${x.elem.fansi} -> ${x.weight}"
    }

  implicit def fd[U: FansiShow]: FansiShow[FiniteDistribution[U]] =
    new FansiShow[FiniteDistribution[U]] {
      def show(x: FiniteDistribution[U]) =
        (x.flatten.sort map (_.fansi)).pmf.fansi
    }
}

import upickle.Js
import upickle.default._

object JsonTranslate{
  import TermPatterns._


  val termToJson =
    Translator.Empty[Term, Js.Value] || formalAppln >>> {
      case (func, arg) => Js.Obj("intro" -> Js.Str("appln"), "func" -> func, "arg" -> arg)
    } || funcTyp >>> {
      case (dom, codom) =>
       Js.Obj("intro" -> Js.Str("func-type"), "dom" -> dom, "codom" -> codom)
    } || lambdaTriple >>> {
      case ((variable, typ), value) =>
      Js.Obj("intro" -> Js.Str("lambda"), "var" -> variable, "type" -> typ, "value" -> value)
    } || piTriple >>> {
      case ((variable, typ), value) =>
      Js.Obj("intro" -> Js.Str("pi"), "var" -> variable, "type" -> typ, "value" -> value)
    } || sigmaTriple >>> {
      case ((variable, typ), value) =>
      Js.Obj("intro" -> Js.Str("sigma"), "var" -> variable, "type" -> typ, "value" -> value)
    } || universe >>> { (n) =>
      Js.Obj("intro" -> Js.Str("universe"), "level" -> Js.Num(n))
    } || symName >>>
      ((s) => Js.Str(s)) ||
      prodTyp >>>
      { case (first, second) =>
        Js.Obj("intro" -> Js.Str("product-type"), "first" -> first, "second" -> second)
      } ||
    absPair >>>       { case (first, second) =>
        Js.Obj("intro" -> Js.Str("pair"), "first" -> first, "second" -> second)
      } ||
      // identityTyp >>> {case ((dom, lhs), rhs) => (lhs ++ LightRed(" = ") ++ rhs ++ " (in " ++ dom ++ ")")} || // invoke if we want expanded equality
      equation >>> { case (lhs, rhs) =>
        Js.Obj("intro" ->  Js.Str("equality"), "lhs" -> lhs, "rhs" -> rhs)} ||
      plusTyp >>>       { case (first, second) =>
        Js.Obj("intro" -> Js.Str("plus-type"), "first" -> first, "second" -> second)
      }||
      indRecFunc >>> {
        case (index, (dom, (codom, defnData))) =>
            Js.Obj("intro" -> Js.Str("indexed-rec-function"), "dom" -> dom, "codom" -> codom, "data" -> Js.Arr(defnData : _*), "index" -> Js.Arr(index : _*))
          } ||
      recFunc >>> {
        case (dom, (codom, defnData)) =>
        Js.Obj("intro" -> Js.Str("rec-function"), "dom" -> dom, "codom" -> codom, "data" -> Js.Arr(defnData : _*))
      } ||
      indInducFunc >>> {
        case (index, (dom, (depcodom, defnData))) =>
        Js.Obj("intro" -> Js.Str("indexed-induc-function"), "dom" -> dom, "depcodom" -> depcodom, "data" -> Js.Arr(defnData : _*), "index" -> Js.Arr(index : _*))
      }  ||
      inducFunc >>> {
        case (dom, (depcodom, defnData)) =>
        Js.Obj("intro" -> Js.Str("induc-function"), "dom" -> dom, "depcodom" -> depcodom, "data" -> Js.Arr(defnData : _*))
      }

      implicit val TermWriter = upickle.default.Writer[Term]{
        case t => termToJson(t).getOrElse(Js.Obj("intro" -> Js.Str("raw"), "text" -> Js.Str(t.toString)))
      }

      import provingground.{TermLang => T}

      def jsonToTerm(js: Js.Value) : Option[Term] = js("intro").str match {
        case "appln" =>
          for (func <- jsonToTerm(js("func")); arg <- jsonToTerm(js("arg")); res <- T.appln(func, arg)) yield res
        case "func-type" =>
          for (dom <- jsonToTerm(js("dom")); codom <- jsonToTerm(js("codom"))) yield
            dom.asInstanceOf[Typ[Term]] ->: codom.asInstanceOf[Typ[Term]]
        case "lambda" =>
          for (variable <- jsonToTerm(js("var")); value <- jsonToTerm(js("value"))) yield
            variable :~> value
        case "pi" =>
          for (variable <- jsonToTerm(js("var")); value <- jsonToTerm(js("value"))) yield
            pi(variable)(value.asInstanceOf[Typ[Term]])
        case "sigma" =>
          for (variable <- jsonToTerm(js("var")); value <- jsonToTerm(js("value"))) yield
            sigma(variable)(value.asInstanceOf[Typ[Term]])
        case "universe" =>
          Some(Universe(js("level").num.toInt))
        case "product-type" =>
          for (dom <- jsonToTerm(js("first")); codom <- jsonToTerm(js("second"))) yield
            ProdTyp(dom.asInstanceOf[Typ[Term]], codom.asInstanceOf[Typ[Term]])
        case "pair" =>
          for (first <- jsonToTerm(js("first")); second <- jsonToTerm(js("second"));
            res <- T.pair(first, second)) yield res
        case "equality" =>
          for (lhs <- jsonToTerm(js("lhs")); rhs <- jsonToTerm(js("rhs"))) yield
            lhs =:= rhs

      }

}
