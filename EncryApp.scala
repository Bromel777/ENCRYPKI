package encry

import com.google.common.primitives._
import scorex.crypto.hash.{Digest32, Sha256}
import scorex.core.block.Block._
import scorex.core.{ModifierId, ModifierTypeId}
import scorex.core.app.{Application, Version}
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.crypto.encode.Base16
import encry.modifiers.history.EncryBlockHeader
import encry.modifiers.mempool.EncryPaymentTransaction
import scorex.core.transaction.proof.Signature25519
import scorex.crypto.authds.ADKey
import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey, Signature}

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
//    99.toByte, ModifierId @@ Longs.toByteArray(999L), Digest32 @@ Array[Byte](32), 898989L, 0, targetedDiff = 2)
//
//  println("Block Hash > " + Base16.encode(block.powHash))
//  println("Nonce > " + block.nonce)

  val OutputNull = IndexedSeq((ADKey @@ "firstTransaction".getBytes))
  val keyPair = Curve25519.createKeyPair("Bars".getBytes())
  val pubKey : PublicKey = keyPair._2
  val priKey : PrivateKey = keyPair._1
  val prop = PublicKey25519Proposition(pubKey)
  val sigNull = Signature25519(Curve25519.sign(priKey,"firstTransaction".getBytes))

  val sigNullSeq : IndexedSeq[Signature25519] = IndexedSeq(sigNull)

  //val InputNull = IndexedSeq((PublicKey25519Proposition(PublicKey @@ Longs.toByteArray(123L)),12L))
  val InputNull = IndexedSeq((prop,12L))
  val BaseTX = EncryPaymentTransaction(12,123L,OutputNull,sigNullSeq,InputNull)

  for(a <- BaseTX.unlockers){
    println(a.boxKey)
  }

  def forceStopApplication(code: Int = 1): Unit =
    new Thread(() => System.exit(code), "encry-shutdown-thread").start()
}
