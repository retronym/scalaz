package scalaz

// We can't make this contra-variant and have a specific instance for Stream with
// the current rules for implicit prioritization in Scala.
trait FoldRight[F[_]] {
  def foldRight[A, B](t: F[A], b: B, f: (A, => B) => B): B
}

object FoldRight {
  import Scalaz._

  implicit def IdentityFoldRight = new FoldRight[Identity] {
    def foldRight[A, B](t: Identity[A], b: B, f: (A, => B) => B) = f(t.value, b)
  }

  def IterableFoldRight[I[X] <: Iterable[X]]: FoldRight[I] = new FoldRight[I] {
    def foldRight[A, B](t: I[A], b: B, f: (A, => B) => B): B = t.foldRight(b)(f(_, _))
  }

  implicit val IterableFoldRight0: FoldRight[Iterable] = IterableFoldRight[Iterable]

  implicit def ListFoldRight: FoldRight[List] = IterableFoldRight[List]

  implicit def VectorFoldRight: FoldRight[Vector] = IterableFoldRight[Vector]

  import collection.mutable.Buffer

  implicit def BufferFoldRight: FoldRight[Buffer] = IterableFoldRight[Buffer]

  implicit def SetFoldRight: FoldRight[Set] = IterableFoldRight[Set]

  implicit def SeqFoldRight: FoldRight[Seq] = IterableFoldRight[Seq]

  implicit def NonEmptyListFoldRight = new FoldRight[NonEmptyList] {
    def foldRight[A, B](t: NonEmptyList[A], b: B, f: (A, => B) => B) = IterableFoldRight.foldRight(t.list, b, f)
  }

  implicit def StateFoldRight = new FoldRight[PartialApply1Of2[State, Unit]#Apply] {
    def foldRight[A, B](t: State[Unit, A], b: B, f: (A, => B) => B) = f(t(())._2, b)
  }

  implicit def Tuple1FoldRight = new FoldRight[Tuple1] {
    def foldRight[A, B](t: Tuple1[A], b: B, f: (A, => B) => B) = f(t._1, b)
  }

  implicit def Function0FoldRight = new FoldRight[Function0] {
    def foldRight[A, B](t: Function0[A], b: B, f: (A, => B) => B) = f(t.apply, b)
  }

  implicit def OptionFoldRight = new FoldRight[Option] {
    def foldRight[A, B](t: Option[A], b: B, f: (A, => B) => B) = t match {
      case Some(a) => f(a, b)
      case None => b
    }
  }

  implicit def TreeFoldRight: FoldRight[Tree] = new FoldRight[Tree] {
    def foldRight[A, B](t: Tree[A], b: B, f: (A, => B) => B): B = {
      t.foldMap((a: A) => ((f.curried(a)(_: B)): Endo[B])) apply b
    }
  }

  implicit def ZipperFoldRight: FoldRight[Zipper] = new FoldRight[Zipper] {
    def foldRight[A, B](t: Zipper[A], b: B, f: (A, => B) => B): B =
      t.lefts.foldLeft(Stream.cons(t.focus, t.rights).foldRight(b)(f(_, _)))((f.flip)(_, _))
  }

  implicit def ZipStreamFoldRight: FoldRight[ZipStream] = new FoldRight[ZipStream] {
    def foldRight[A, B](t: ZipStream[A], b: B, f: (A, => B) => B): B = StreamFoldRight.foldRight(t.value, b, f)
  }

  implicit def GenericArrayFoldRight: FoldRight[GArray] = IterableFoldRight[GArray]

  implicit def EitherLeftFoldRight[X] = new FoldRight[PartialApply1Of2[Either.LeftProjection, X]#Flip] {
    def foldRight[A, B](e: Either.LeftProjection[A, X], b: B, f: (A, => B) => B) = OptionFoldRight.foldRight(e.toOption, b, f)
  }

  implicit def EitherRightFoldRight[X] = new FoldRight[PartialApply1Of2[Either.RightProjection, X]#Apply] {
    def foldRight[A, B](e: Either.RightProjection[X, A], b: B, f: (A, => B) => B) = OptionFoldRight.foldRight(e.toOption, b, f)
  }

  implicit def ValidationFoldRight[X] = new FoldRight[PartialApply1Of2[Validation, X]#Apply] {
    def foldRight[A, B](e: Validation[X, A], b: B, f: (A, => B) => B) = e match {
      case Success(a) => f(a, b)
      case Failure(_) => b
    }
  }

  implicit def ValidationFailureFoldRight[X] = new FoldRight[PartialApply1Of2[FailProjection, X]#Flip] {
    def foldRight[A, B](e: FailProjection[A, X], b: B, f: (A, => B) => B) = e.validation match {
      case Success(_) => b
      case Failure(e) => f(e, b)
    }
  }

  implicit def StreamFoldRight: FoldRight[Stream] = new FoldRight[Stream] {
    def foldRight[A, B](t: Stream[A], b: B, f: (A, => B) => B): B =
      if(t.isEmpty)
        b
      else
        f(t.head, foldRight(t.tail, b, f))
  }

  implicit def JavaIterableFoldRight[A] = new FoldRight[java.lang.Iterable] {
    def foldRight[A, B](t: java.lang.Iterable[A], b: B, f: (A, => B) => B) = {
      val i = new Iterable[A] {
        override def iterator = new Iterator[A] {
          val k = t.iterator
          def hasNext = k.hasNext
          def next = k.next
        }
      }
      IterableFoldRight[Iterable].foldRight[A, B](i, b, f)
    }
  }

  import java.util.concurrent.Callable

  implicit def CallableFoldRight = new FoldRight[Callable] {
    def foldRight[A, B](t: Callable[A], b: B, f: (A, => B) => B) = f(t.call, b)
  }
}
