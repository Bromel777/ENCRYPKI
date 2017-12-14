package encry.nodeView.mempool


import encry.modifiers.mempool.{EncryBaseTransaction, EncryPaymentTransaction}
import encry.nodeView.mempool.EncryMempool._
import scorex.core.{EphemerealNodeViewModifier, ModifierId, NodeViewComponent}
import scorex.core.transaction.{MemoryPool, Transaction}
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.crypto.hash.Digest32

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.HashSet.HashTrieSet
import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Success, Try}

class EncryMempool private[mempool](val unconfirmed: TrieMap[TxKey, EphemerealNodeViewModifier],val boxes : mutable.HashSet[Int])
    //extends MemoryPool[Transaction[_], EncryMempool] {
  extends NodeViewComponent{

  override type NVCT = EncryMempool

  /**
    * Map stores current state of waiting for query building
    * value - promise of result and set of all transactions of request
    * key - set of transactions that are waiting for the assembly
    */
  private[mempool] var waitedForAssembly: Map[Set[TxKey], (Promise[MemPoolResponse], Seq[ModifierId])] = Map.empty

  private def key(id: ModifierId): TxKey = {
    println(new mutable.WrappedArray.ofByte(id))
    new mutable.WrappedArray.ofByte(id)
  }

   def getById(id: ModifierId): Option[EphemerealNodeViewModifier] = unconfirmed.get(key(id))

   def contains(id: ModifierId): Boolean = unconfirmed.contains(key(id))

   def getAll(ids: Seq[ModifierId]): Seq[EphemerealNodeViewModifier] = ids.flatMap(getById)

   def put(tx: EphemerealNodeViewModifier): Try[EncryMempool] = {
    put(Seq(tx))
  }

   def put(txs: Iterable[EphemerealNodeViewModifier]): Try[EncryMempool] = Try {
    txs.foreach(tx => {

    })
    //todo check validity
    putWithoutCheck(txs)
  }

   def putWithoutCheck(txs: Iterable[EphemerealNodeViewModifier]): EncryMempool = {
    txs.foreach(tx => tx match {
      case tx:EncryPaymentTransaction =>{
            println("Add:" + tx.hashNoNonces)
            unconfirmed.put(key(ModifierId @@ tx.trxHash), tx)
      }
      case _ => println("Incorrect Transaction")

    })
//    println("Add:" + tx.hashNoNonces)
//    unconfirmed.put(key(ModifierId @@ tx.trxHash), tx)
    //completeAssembly(txs)
    //todo cleanup?
    this
  }

   def remove(tx: EphemerealNodeViewModifier): EncryMempool = {
    unconfirmed.remove(
      tx match{
        case tx:EncryPaymentTransaction =>{
          key(ModifierId @@ tx.trxHash)
        }
      }

    )
    this
  }

   def take(limit: Int): Iterable[EphemerealNodeViewModifier] =
    unconfirmed.values.toSeq.take(limit)

   def filter(condition: (EphemerealNodeViewModifier) => Boolean): EncryMempool = {
    unconfirmed.retain { (k, v) =>
      condition(v)
    }
    this
  }

   def size: Int = unconfirmed.size

  private def completeAssembly(txs: Iterable[EphemerealNodeViewModifier]): Unit = synchronized {
    val txsIds = txs.map(
      tx => tx match{
        case tx:EncryPaymentTransaction =>{
          key(ModifierId @@ tx.trxHash)
        }
      }
    )
    val newMap = waitedForAssembly.flatMap(p => {
      val ids = p._1
      val newKey = ids -- txsIds
      // filtering fully-built queries and completing of a promise
      if (newKey.isEmpty) {
        val (promise, allIds) = p._2
        promise complete Success(allIds.map(id => getById(id).get))
        None
      } else {
        Some(newKey -> p._2)
      }
    })
    waitedForAssembly = newMap
  }

  def waitForAll(ids: MemPoolRequest): Future[MemPoolResponse] = synchronized {
    val promise = Promise[Seq[EphemerealNodeViewModifier]]
    waitedForAssembly = waitedForAssembly.updated(ids.map(id => key(id)).toSet, (promise, ids))
    promise.future
  }
}

object EncryMempool {
  type TxKey = scala.collection.mutable.WrappedArray.ofByte

  type MemPoolRequest = Seq[ModifierId]

  type MemPoolResponse = Seq[EphemerealNodeViewModifier]

  def empty: EncryMempool = new EncryMempool(TrieMap.empty,mutable.HashSet.empty)
}