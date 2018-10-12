//package annette.imc
//
//import java.time.ZonedDateTime
//import java.util.UUID
//
//import akka.actor.ActorSystem
//import akka.pattern.ask
//import akka.testkit.TestKit
//import annette.core.test.PersistenceSpec
//import annette.core.utils._
//import annette.imc.ApsActor._
//import annette.imc.model.ApFile._
//import annette.imc.model._
//
//class ApsActorSpec extends TestKit(ActorSystem("ApsActorSpec"))
//  with PersistenceSpec
//  with NewAps {
//  "A ApsActor" when receive {
//    "CreateCmd" must {
//      "createUser new ap" in {
//        val user = UUID.randomUUID()
//        for {
//          a <- newAps()
//          createUser <- ask(a, CreateCmd(user))
//          create1 <- ask(a, CreateCmd(user))
//          create2 <- ask(a, CreateCmd(user))
//
//          findAll <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps)
//          findById <- ask(a, GetApById(findAll.toList.head.id)).mapTo[ApFound].map(_.ap)
//          findByWrongId <- ask(a, GetApById(UUID.randomUUID()))
//
//        } yield {
//
//          findAll.size shouldBe 3
//          findById.id shouldBe findAll.toList.head.id
//          findByWrongId shouldBe ApNotExists
//          findById.apStatus.userId shouldBe Some(user)
//          findById.history.size shouldBe 1
//          findById.expertise.experts shouldBe Set()
//          findById.expertise.bulletins shouldBe Map()
//          findById.criterions shouldBe Map()
//          findById.apFiles shouldBe Map()
//          findById.apData.entityName shouldBe None
//        }
//      }
//
//      "filling ApData" in {
//        val user = UUID.randomUUID()
//
//        def update1(apId: UUID) = UpdateAp(id = apId, entityName = Some(ApString("Ай-би-эм", "IBM")))
//        def update2(apId: UUID) = UpdateAp(id = apId, personName = Some(ApString("Кузнецов Иван Иванович", "Smith Ivan")))
//        def update3(apId: UUID) = UpdateAp(id = apId, personPosition = Some(ApString("Директор", "Director")))
//        def update4(apId: UUID) = UpdateAp(id = apId, personEmail = Some("ibm@mail.ru"))
//        def update5(apId: UUID) = UpdateAp(id = apId, personTel = Some("111-11-11"))
//        def update6(apId: UUID) = UpdateAp(id = apId, country = Some("RU"))
//        //        def update7(apId: UUID) = UpdateAp(id = apId, operationTypes =
//        //          Some(Set(
//        //            OperationType(OperationType.CLINIC),
//        //            OperationType(OperationType.SCIENCE, Some(ApString("Ля-ля-ля", "Blah-blah-blah"))))))
//        //        def update8(apId: UUID) = UpdateAp(id = apId, participation = Some(Participation(Participation.OPERATORONLY)))
//        def update9(apId: UUID) = UpdateAp(id = apId, financing = Some(Set(Financing(Financing.LOANS))))
//        def update10(apId: UUID) = UpdateAp(id = apId, isForLong = Some(false))
//        def update11(apId: UUID) = UpdateAp(id = apId, isForLong = Some(true))
//        def update12(apId: UUID) = UpdateAp(
//          id = apId,
//          entityName = Some(ApString("Яблоко", "Apple")),
//          personEmail = Some("apple@mail.ru"))
//        def update13(apId: UUID) = UpdateAp(id = apId, purpose = Some("med"))
//        def update14(apId: UUID) = UpdateAp(id = apId, financing = Some(Set(Financing(Financing.LOANS), Financing(Financing.OWNFUNDS))))
//        def update15(apId: UUID) = UpdateAp(id = apId, name = Some(ApString("lalala", "hihihi")))
//        def update16(apId: UUID) = UpdateAp(id = apId, capital = Some("1000000$"))
//        def update18(apId: UUID) = UpdateAp(id = apId, address = Some("Moscow"))
//        def update17(apId: UUID) = UpdateAp(id = apId, applicantInfo = Some(ApString("lalala", "hihihi")))
//        val wrongIdAp = UpdateAp(id = UUID.randomUUID(), isForLong = Some(true))
//
//        for {
//          a <- newAps()
//          createUser <- ask(a, CreateCmd(user))
//          id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//          u1 <- ask(a, FillingFormCmd(update1(id)))
//          u2 <- ask(a, FillingFormCmd(update2(id)))
//          u3 <- ask(a, FillingFormCmd(update3(id)))
//          u4 <- ask(a, FillingFormCmd(update4(id)))
//          u5 <- ask(a, FillingFormCmd(update5(id)))
//          u6 <- ask(a, FillingFormCmd(update6(id)))
//          //          u7 <- ask(a, FillingFormCmd(update7(id)))
//          //          u8 <- ask(a, FillingFormCmd(update8(id)))
//          u9 <- ask(a, FillingFormCmd(update9(id)))
//          u10 <- ask(a, FillingFormCmd(update10(id)))
//          apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          u11 <- ask(a, FillingFormCmd(update11(id)))
//          apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          u12 <- ask(a, FillingFormCmd(update12(id)))
//          u13 <- ask(a, FillingFormCmd(update13(id)))
//          u14 <- ask(a, FillingFormCmd(update14(id)))
//          u15 <- ask(a, FillingFormCmd(update15(id)))
//          u16 <- ask(a, FillingFormCmd(update16(id)))
//          u17 <- ask(a, FillingFormCmd(update17(id)))
//          u18 <- ask(a, FillingFormCmd(update18(id)))
//          apFound2 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          wrongId <- ask(a, FillingFormCmd(wrongIdAp))
//
//        } yield {
//          apFound.id shouldBe id
//          apFound.apData.entityName.map(_.en) shouldBe Some("IBM")
//          apFound.apData.personName.map(_.ru) shouldBe Some("Кузнецов Иван Иванович")
//          apFound.apData.personPosition.map(_.ru) shouldBe Some("Директор")
//          apFound.apData.personEmail shouldBe Some("ibm@mail.ru")
//          apFound.apData.personTel shouldBe Some("111-11-11")
//          apFound.apData.country shouldBe Some("RU")
//          //          apFound.apData.operationTypes.map(_.size) shouldBe Some(2)
//          //          apFound.apData.operationTypes.flatMap(_.find(
//          //            x => x.nameMessage == "clinic").map(y => y.getMedType)) shouldBe Some("Medicine")
//          //          apFound.apData.operationTypes.flatMap(_.find(
//          //            x => x.nameMessage == "science")
//          //            .flatMap(y => y.details.map(_.en))) shouldBe Some("Blah-blah-blah")
//          apFound.apData.financing.map(_.head.nameMessage) shouldBe Some("loans")
//          //          apFound.apData.participation.map(_.nameMessage) shouldBe Some("operatoronly")
//          apFound.apData.isForLong shouldBe Some(false)
//          apFound1.apData.isForLong shouldBe Some(true)
//
//          apFound2.apData.entityName.map(_.en) shouldBe Some("Apple")
//          apFound2.apData.personName.map(_.ru) shouldBe Some("Кузнецов Иван Иванович")
//          apFound2.apData.personPosition.map(_.ru) shouldBe Some("Директор")
//          apFound2.apData.personEmail shouldBe Some("apple@mail.ru")
//          apFound2.apData.purpose shouldBe Some("med")
//          apFound2.apData.name.map(_.ru) shouldBe Some("lalala")
//          apFound2.apData.name.map(_.en) shouldBe Some("hihihi")
//          apFound2.apData.capital shouldBe Some("1000000$")
//          apFound2.apData.address shouldBe Some("Moscow")
//          apFound2.apData.financing.map(_.size) shouldBe Some(2)
//          apFound2.apData.applicantInfo.map(_.ru) shouldBe Some("lalala")
//          apFound2.apData.applicantInfo.map(_.en) shouldBe Some("hihihi")
//          wrongId shouldBe ApNotExists
//        }
//      }
//
//      "adding / editing/ removing files" in {
//        val user = UUID.randomUUID()
//
//        val file1 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Договор.pdf",
//          fileType = FileType(FileType.Fl160Files))
//        val file2 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Устав.pdf",
//          fileType = FileType(FileType.ChartFile))
//
//        val file3 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Chart.pdf",
//          fileType = FileType(FileType.ChartFile),
//          lang = "En")
//
//        for {
//          a <- newAps()
//          createUser <- ask(a, CreateCmd(user))
//          id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//          f1 <- ask(a, AddFileCmd(id, file1))
//          f2 <- ask(a, AddFileCmd(id, file2))
//          f3 <- ask(a, AddFileCmd(id, file3))
//
//          apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//
//          updateUser <- ask(a, UpdateFileCmd(id, file1.copy(comments = "Blah-blah-blah")))
//
//          apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          wrongUpdate <- ask(a, UpdateFileCmd(UUID.randomUUID(), file1.copy(comments = "Blah-blah-blah")))
//          wrongUpdate1 <- ask(a, UpdateFileCmd(id, file1.copy(comments = "Blah-blah-blah", id = UUID.randomUUID())))
//
//          remove <- ask(a, RemoveFileCmd(id, file1.id))
//          apFound3 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//
//        } yield {
//
//          f1 shouldBe Done
//          f2 shouldBe Done
//          f3 shouldBe Done
//
//          apFound.apFiles.size shouldBe 3
//          apFound.apFiles.get(file1.id).map(_.fileType) shouldBe Some(FileType(FileType.Fl160Files))
//          apFound.apFiles.get(file1.id).map(_.lang) shouldBe Some("Ru")
//          apFound.apFiles.get(file3.id).map(_.lang) shouldBe Some("En")
//          updateUser shouldBe Done
//          apFound1.apFiles.get(file1.id).map(_.comments) shouldBe Some("Blah-blah-blah")
//          apFound1.apFiles.size shouldBe 3
//          wrongUpdate shouldBe ApNotExists
//          wrongUpdate1 shouldBe FileNotExists
//          remove shouldBe Done
//          apFound3.apFiles.get(file1.id) shouldBe None
//          apFound3.apFiles.size shouldBe 2
//
//        }
//      }
//
//      "updating criterions" in {
//        val user = UUID.randomUUID()
//
//        val file1 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Договор.pdf",
//          fileType = FileType(FileType.Fl160Files))
//        val file2 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Устав.pdf",
//          fileType = FileType(FileType.ChartFile))
//
//        val file3 = ApFile(
//          id = UUID.randomUUID(),
//          name = "Chart.pdf",
//          fileType = FileType(FileType.ChartFile),
//          lang = "En")
//
//        for {
//          a <- newAps()
//          createUser <- ask(a, CreateCmd(user))
//          id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//          f1 <- ask(a, AddFileCmd(id, file1))
//          f2 <- ask(a, AddFileCmd(id, file2))
//          f3 <- ask(a, AddFileCmd(id, file3))
//          apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          update1 <- ask(a, UpdateCriterionCmd(id, UpdateCriterion(1, Some("Ля-ля-ля"))))
//          wrongUpdate <- ask(a, UpdateCriterionCmd(UUID.randomUUID(), UpdateCriterion(2, Some("Жужужу"))))
//          apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          update2 <- ask(a, UpdateCriterionCmd(id, UpdateCriterion(1, None, Some("Blah-blah-blah"))))
//          apFound2 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          update3 <- ask(a, UpdateCriterionCmd(id, UpdateCriterion(2, Some("Жужужу"), Some("Blah"))))
//          apFound3 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          adfile1 <- ask(a, AddCriterionFileCmd(id, 1, file1.id))
//          adfile2 <- ask(a, AddCriterionFileCmd(id, 1, file2.id))
//          adfile3 <- ask(a, AddCriterionFileCmd(id, 2, file3.id))
//          adfile4 <- ask(a, AddCriterionFileCmd(id, 2, file2.id))
//          criterion1 <- ask(a, GetCriterionById(id, 1)).mapTo[CriterionFound].map(_.criterion)
//          criterion2 <- ask(a, GetCriterionById(id, 1)).mapTo[CriterionFound].map(_.criterion)
//          removeFile <- ask(a, RemoveCriterionFileCmd(id, 1, file1.id))
//          removeFile1 <- ask(a, RemoveCriterionFileCmd(id, 1, file3.id))
//          removeWrongFile <- ask(a, RemoveCriterionFileCmd(id, 1, UUID.randomUUID()))
//          criterion1_1 <- ask(a, GetCriterionById(id, 1)).mapTo[CriterionFound].map(_.criterion)
//          end <- ask(a, FinishCriterionCmd(id, 1))
//          criterion1_2 <- ask(a, GetCriterionById(id, 1)).mapTo[CriterionFound].map(_.criterion)
//          toggle <- ask(a, FinishCriterionCmd(id, 1))
//          criterion1_3 <- ask(a, GetCriterionById(id, 1)).mapTo[CriterionFound].map(_.criterion)
//          clean <- ask(a, CleanCriterionCmd(id, 2))
//          criterion2_1 <- ask(a, GetCriterionById(id, 2))
//        } yield {
//
//          apFound.criterions.isEmpty shouldBe true
//          update1 shouldBe Done
//          wrongUpdate shouldBe ApNotExists
//          apFound1.criterions.size shouldBe 1
//          apFound1.criterions.get(1).map(_.description.ru) shouldBe Some("Ля-ля-ля")
//          apFound1.criterions.get(1).map(_.description.en) shouldBe Some("")
//          apFound1.criterions.get(1).map(_.isFinished) shouldBe Some(false)
//          apFound2.criterions.get(1).map(_.description.ru) shouldBe Some("Ля-ля-ля")
//          apFound2.criterions.get(1).map(_.description.en) shouldBe Some("Blah-blah-blah")
//          apFound3.criterions.get(2).map(_.description.ru) shouldBe Some("Жужужу")
//          apFound3.criterions.get(2).map(_.description.en) shouldBe Some("Blah")
//          adfile1 shouldBe Done
//          adfile2 shouldBe Done
//          adfile3 shouldBe Done
//          adfile4 shouldBe Done
//          criterion1.attachment.size shouldBe 2
//          criterion2.attachment.size shouldBe 2
//          criterion1.attachment.contains(file1.id) shouldBe true
//          criterion1.attachment.contains(file3.id) shouldBe false
//          removeFile shouldBe Done
//          removeFile1 shouldBe Done //ничего не удаляет но и не ругается
//          removeWrongFile shouldBe FileNotExists
//          criterion1_1.attachment.size shouldBe 1
//          end shouldBe Done
//          criterion1_2.isFinished shouldBe true
//          toggle shouldBe Done
//          criterion1_3.isFinished shouldBe false
//          clean shouldBe Done
//          criterion2_1 shouldBe CriterionNotExists
//        }
//      }
//
//      "adding / removing experts" in {
//        val user = UUID.randomUUID()
//        val (expert1, expert2, expert3) = (UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
//        def update1(apId: UUID) = UpdateAp(id = apId, entityName = Some(ApString("Ай-би-эм", "IBM")))
//
//        for {
//          a <- newAps()
//          createUser <- ask(a, CreateCmd(user))
//          id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//          add1 <- ask(a, AddExpertCmd(id, expert1))
//          empty <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap.expertise.experts)
//          u1 <- ask(a, FillingFormCmd(update1(id)))
//          add2 <- ask(a, AddExpertCmd(id, expert1))
//          add3 <- ask(a, AddExpertCmd(id, expert2))
//          add4 <- ask(a, AddExpertCmd(id, expert3))
//          apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          remove <- ask(a, RemoveExpertCmd(id, expert3))
//          apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//          wrongRemove <- ask(a, RemoveExpertCmd(id, UUID.randomUUID()))
//
//        } yield {
//
//          //          add1 shouldBe EnterEntityNameFirst
//          //          empty.isEmpty shouldBe true
//          add2 shouldBe Done
//          add3 shouldBe Done
//          add4 shouldBe Done
//          apFound.expertise.experts.size shouldBe 3
//          apFound.expertise.bulletins.size shouldBe 3
//          apFound.expertise.bulletins.get(expert1).isDefined shouldBe true
//          remove shouldBe Done
//          apFound1.expertise.experts.size shouldBe 2
//          apFound1.expertise.bulletins.size shouldBe 2
//          apFound1.expertise.bulletins.get(expert3).isDefined shouldBe false
//          wrongRemove shouldBe NotFound
//        }
//      }
//    }
//
//    "updating bulletins" in {
//      val user = UUID.randomUUID()
//      val expert1 = UUID.randomUUID()
//      val expert2 = UUID.randomUUID()
//      val expert3 = UUID.randomUUID()
//
//      val update2 = UpdateBulletin(expertId = expert1, criterions = Some(Map(1 -> Vote(2, "Blah", "Fa"))))
//      val update3 = UpdateBulletin(expertId = expert1, scores = Some(Scores(9, 6, 8)))
//      val update4 = UpdateBulletin(expertId = expert1, finalResult = Some(false))
//
//      for {
//        a <- newAps()
//        createUser <- ask(a, CreateCmd(user))
//        id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//        add2 <- ask(a, AddExpertCmd(id, expert1))
//        add3 <- ask(a, AddExpertCmd(id, expert2))
//        add4 <- ask(a, AddExpertCmd(id, expert3))
//        u2 <- ask(a, UpdateBulletinCmd(id, update2))
//        u3 <- ask(a, UpdateBulletinCmd(id, update3))
//        u4 <- ask(a, UpdateBulletinCmd(id, update4))
//        apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//        vote <- ask(a, VoteCmd(id, expert1))
//        apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//      } yield {
//
//        add2 shouldBe Done
//        add3 shouldBe Done
//        add4 shouldBe Done
//        u2 shouldBe Done
//        u3 shouldBe Done
//        u4 shouldBe Done
//
//        apFound.expertise.bulletins.get(expert1)
//          .map(_.criterions.size) shouldBe Some(1)
//        apFound.expertise.bulletins.get(expert1)
//          .flatMap(_.criterions.get(1)).map(_.decision) shouldBe Some(2)
//        vote shouldBe Done
//        apFound1.expertise.bulletins.get(expert1).map(_.isFinished) shouldBe Some(true)
//      }
//    }
//
//    "changing status" in {
//      val user = UUID.randomUUID()
//      val expert1 = UUID.randomUUID()
//      val expert2 = UUID.randomUUID()
//      def fill(apId: UUID) = UpdateAp(id = apId, entityName = Some(ApString("Ай-би-эм", "IBM")))
//      for {
//        a <- newAps()
//        createUser <- ask(a, CreateCmd(user))
//        id <- ask(a, FindAps(SearchParams())).mapTo[ApsFound].map(_.aps.head.id)
//        f <- ask(a, FillingFormCmd(fill(id)))
//        add1 <- ask(a, AddExpertCmd(id, expert1))
//        add2 <- ask(a, AddExpertCmd(id, expert2))
//        apFound <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//        filled <- ask(a, ChangeStatusCmd(id, ApStatus(ApStatus.FILLED, ZonedDateTime.now(), Some(user))))
//        apFound1 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//        filling <- ask(a, ChangeStatusCmd(id, ApStatus(ApStatus.FILLING, ZonedDateTime.now(), Some(user), Some("!"))))
//        apFound2 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//        filled1 <- ask(a, ChangeStatusCmd(id, ApStatus(ApStatus.FILLED, ZonedDateTime.now(), Some(user))))
//        onExpertise <- ask(a, ChangeStatusCmd(id, ApStatus(ApStatus.ONEXPERTISE, ZonedDateTime.now(), Some(user))))
//        apFound3 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//        vote1 <- ask(a, VoteCmd(id, expert1))
//        vote2 <- ask(a, VoteCmd(id, expert2))
//        apFound4 <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap)
//      } yield {
//
//        apFound.apStatus.nameMessage shouldBe ApStatus.FILLING
//        apFound.history.size shouldBe 1
//        f shouldBe Done
//        add2 shouldBe Done
//        add1 shouldBe Done
//
//        filled shouldBe Done
//        apFound1.history.size shouldBe 2
//        apFound1.apStatus.nameMessage shouldBe ApStatus.FILLED
//        apFound1.apStatus.userId shouldBe Some(user)
//
//        filling shouldBe Done
//        apFound2.history.size shouldBe 3
//        apFound2.apStatus.nameMessage shouldBe ApStatus.FILLING
//        apFound2.apStatus.comment shouldBe Some("!")
//
//        filled1 shouldBe Done
//
//        onExpertise shouldBe Done
//        apFound3.history.size shouldBe 5
//        apFound3.apStatus.nameMessage shouldBe ApStatus.ONEXPERTISE
//
//        vote1 shouldBe Done
//        vote2 shouldBe Done
//
//        apFound4.apStatus.nameMessage shouldBe ApStatus.ACCOMPLISHED
//
//        apFound4.history.size shouldBe 6
//
//      }
//    }
//
//    "searching ApData" in {
//      val user = UUID.randomUUID()
//
//      def update1(apId: UUID) = UpdateAp(id = apId, entityName = Some(ApString("Ай-би-эм", "IBM")))
//      def update2(apId: UUID) = UpdateAp(id = apId, personName = Some(ApString("Кузнецов Иван Иванович", "Smith Ivan")))
//
//      def update3(apId: UUID) = UpdateAp(id = apId, entityName = Some(ApString("Эппл", "Apple")))
//      def update4(apId: UUID) = UpdateAp(id = apId, personName = Some(ApString("Петров Иван Иванович", "Petrov Ivan")))
//
//      val searchParams = SearchParams(Some("Кузнецов"))
//      val searchParams2 = SearchParams(Some("Водкин"))
//      val searchParams3 = SearchParams(Some("Кузне"))
//      val searchParams4 = SearchParams(Some("Appl"))
//      val searchParams5 = SearchParams(Some("appl"))
//      val searchParams6 = SearchParams(Some("APPl"))
//      val searchParams7 = SearchParams(Some("кузне"))
//      val searchParams8 = SearchParams(Some("КУзне"))
//      val searchParams9 = SearchParams()
//      val searchParams10 = SearchParams(None, Some(ApStatus.FILLED))
//      val searchParams11 = SearchParams(None, Some(ApStatus.FILLING))
//      val searchParams12 = SearchParams(Some("КУзне"), Some(ApStatus.FILLED))
//
//      for {
//        a <- newAps()
//        id1 <- ask(a, CreateCmd(user)).mapTo[Created].map(_.id)
//        id2 <- ask(a, CreateCmd(user)).mapTo[Created].map(_.id)
//        id3 <- ask(a, CreateCmd(user)).mapTo[Created].map(_.id)
//        u1 <- ask(a, FillingFormCmd(update1(id1)))
//        u2 <- ask(a, FillingFormCmd(update2(id1)))
//        u3 <- ask(a, FillingFormCmd(update3(id2)))
//        u4 <- ask(a, FillingFormCmd(update4(id2)))
//
//        filled <- ask(a, ChangeStatusCmd(id1, ApStatus(ApStatus.FILLED, ZonedDateTime.now(), Some(user))))
//
//        s1 <- ask(a, FindAps(searchParams)).mapTo[ApsFound].map(_.aps.size)
//        s2 <- ask(a, FindAps(searchParams2)).mapTo[ApsFound].map(_.aps.size)
//        s3 <- ask(a, FindAps(searchParams3)).mapTo[ApsFound].map(_.aps.size)
//        s4 <- ask(a, FindAps(searchParams4)).mapTo[ApsFound].map(_.aps.size)
//        s5 <- ask(a, FindAps(searchParams5)).mapTo[ApsFound].map(_.aps.size)
//        s6 <- ask(a, FindAps(searchParams6)).mapTo[ApsFound].map(_.aps.size)
//        s7 <- ask(a, FindAps(searchParams7)).mapTo[ApsFound].map(_.aps.size)
//        s8 <- ask(a, FindAps(searchParams8)).mapTo[ApsFound].map(_.aps.size)
//        s9 <- ask(a, FindAps(searchParams9)).mapTo[ApsFound].map(_.aps.size)
//        s10 <- ask(a, FindAps(searchParams10)).mapTo[ApsFound].map(_.aps.size)
//        s11 <- ask(a, FindAps(searchParams11)).mapTo[ApsFound].map(_.aps.size)
//        s12 <- ask(a, FindAps(searchParams12)).mapTo[ApsFound].map(_.aps.size)
//
//      } yield {
//        u1 shouldBe Done
//        u2 shouldBe Done
//        u3 shouldBe Done
//        u4 shouldBe Done
//
//        filled shouldBe Done
//        s1 shouldBe 1
//        s2 shouldBe 0
//        s3 shouldBe 1
//        s4 shouldBe 1
//        s5 shouldBe 1
//        s6 shouldBe 1
//        s7 shouldBe 1
//        s8 shouldBe 1
//        s9 shouldBe 3
//        s10 shouldBe 1
//        s11 shouldBe 2
//        s12 shouldBe 1
//      }
//    }
//    "updating PM " in {
//      val user = UUID.randomUUID()
//      val pm = UUID.randomUUID()
//      for {
//        a <- newAps()
//        id <- ask(a, CreateCmd(user)).mapTo[Created].map(_.id)
//        change <- ask(a, ChangeManagerCmd(id, pm))
//        pmId <- ask(a, GetApById(id)).mapTo[ApFound].map(_.ap.projectManager)
//      } yield {
//        change shouldBe Done
//        pmId shouldBe pm
//      }
//    }
//  }
//
//}
