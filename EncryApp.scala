package encry

import com.google.common.primitives._
import scorex.crypto.hash.{Digest32, Sha256}
import scorex.core.block.Block._
import scorex.core.{EphemerealNodeViewModifier, ModifierId, ModifierTypeId}
import scorex.core.app.{Application, Version}
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.crypto.encode.Base16
import encry.modifiers.history.EncryBlockHeader
import encry.modifiers.mempool.EncryPaymentTransaction
import encry.nodeView.mempool.EncryMempool
import org.scalameter.utils.Tree
import scorex.core.transaction.proof.Signature25519
import scorex.crypto.authds.ADKey
import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey, Signature}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

//class EncryApp(args: Seq[String]) extends Application {
//  override type P = PublicKey25519Proposition
//  override type TX = this.type
//  override type PMOD = this.type
//  override type NVHT = this.type
//}

//object EncryApp extends App {
//  new EncryApp(args).run()
object EncryApp extends App{
//  val block = new EncryBlockHeader(
//    99.toByte, ModifierId d@@ Longs.toByteArray(999L), Digest32 @@ Array[Byte](32), 898989L, 0, targetedDiff = 2)
//
//  println("Block Hash > " + Base16.encode(block.powHash))
//  println("Nonce > " + block.nonce)
//
  val InputNullTX1 = IndexedSeq((ADKey @@ "firstOutputIndex".getBytes))
  val InputNullTX2 = IndexedSeq((ADKey @@ "secondOutputIndex".getBytes))
  val keyPair = Curve25519.createKeyPair("Bars".getBytes())
  val pubKey : PublicKey = keyPair._2
  val priKey : PrivateKey = keyPair._1
  val recepientProp = PublicKey25519Proposition(pubKey)
  val senderProp = PublicKey25519Proposition(pubKey)
  val sigTX1 = Signature25519(Curve25519.sign(priKey,"firstTransaction".getBytes))
  val sigTX2 = Signature25519(Curve25519.sign(priKey,"secondTransaction".getBytes))

  //val InputNull = IndexedSeq((PublicKey25519Proposition(PublicKey @@ Longs.toByteArray(123L)),12L))
  val OutputNullTX1 = IndexedSeq((recepientProp,12L))
  val OutputNullTX2 = IndexedSeq((recepientProp,15L))
  val BaseTX1 = EncryPaymentTransaction(senderProp,12L,123L,InputNullTX1,sigTX1,OutputNullTX1)
  val BaseTX2 = EncryPaymentTransaction(senderProp,28L,124L,InputNullTX2,sigTX2,OutputNullTX2)
//
////  for(a <- BaseTX.unlockers){
////    println(a.boxKey)
////  }
//
////  BaseTX1.hashOutputs.foreach(print)
////  println()
////  BaseTX2.hashOutputs.foreach(print)
//
  val txTree = List(BaseTX1,BaseTX2).map{tx => tx.asInstanceOf[EphemerealNodeViewModifier]}
  val tmf = EncryMempool.empty
  tmf.put(txTree)
  val txtr = tmf.unconfirmed
  for((a,b) <- txtr){
      b match {
        case tx : EncryPaymentTransaction =>{
          println(tx.fee)
        }
      }
  }
//  val a :TrieMap[Int, EncryPaymentTransaction] = TrieMap.empty
//  a.put(1,BaseTX1)
//  a.put(2,BaseTX2)
//  for((a,b) <- a){
//    println("in mempool " + b.fee)
//  }

//  println(new mutable.WrappedArray.ofByte(BaseTX2.hashNoNonces))

  def forceStopApplication(code: Int = 1): Unit =
    new Thread(() => System.exit(code), "encry-shutdown-thread").start()
}
