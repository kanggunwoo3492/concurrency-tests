package com.hebeyond.arllimtester.service

import com.hebeyond.arllimtester.persistence.Article
import com.hebeyond.arllimtester.repository.ArticleRepository
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
class ArticleServiceConcurrencyTest @Autowired constructor(
    private val articleRepository: ArticleRepository,
    private val articleService: ArticleService,
) {

    private val service = Executors.newFixedThreadPool(100)


    @Test
    fun test1() {

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                val findAll = articleRepository.findAllByChangeHappened(false)
                val chunked = findAll.chunked(10)
                chunked.parallelStream().forEach { it ->
                    it.forEach {
                        it.changeHappened = true
                        it.readCount++
                    }
                    articleRepository.saveAll(it)
                }
            }
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun test2() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        for (i in 1..100) {
            articles.add(Article(id = i.toLong(), data = "content$i"))
        }

        val chunked = articles.chunked(10)

        chunked.parallelStream().forEach { it ->
            it.forEach {
                it.changeHappened = true
                it.readCount++
            }
            articleRepository.saveAll(it)
        }

        val findAll = articleRepository.findAllByChangeHappened(false)
        assertThat(findAll.size).isEqualTo(0)

        articleRepository.deleteAll()
    }

    @Test
    fun test3() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        for (i in 1..100) {
            articles.add(Article(data = "content$i"))
        }


        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                chunkAndProcess(articles)
                latch.countDown()
            }
        }
        latch.await()

        val findAll = articleRepository.findAllByChangeHappened(false)
        assertThat(findAll.size).isEqualTo(0)
        assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }

        // articleRepository.deleteAll()
    }


    @Test
    fun test4() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        assertThat(articles.size).isEqualTo(100)

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                chunkAndProcess(articles)
                latch.countDown()
            }
        }
        latch.await()

        val findAll = articleRepository.findAllByChangeHappened(false)
        assertThat(findAll.size).isEqualTo(0)
        assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }

        // articleRepository.deleteAll()
    }

    @Test
    fun test5() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        assertThat(articles.size).isEqualTo(100)

        articleRepository.saveAll(articles)

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                val newArticles = articleService.findByChangeHappenedFalse()
                println("newArticles.size: ${newArticles.size}")

                chunkAndProcess(newArticles)

                latch.countDown()
            }
        }
        latch.await()

        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }
    }

    @Test
    fun test6() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        assertThat(articles.size).isEqualTo(100)

        articleRepository.saveAll(articles)

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                myApi()
                latch.countDown()
            }
        }
        latch.await()

        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }
    }


    @Test
    fun test7() {
        articleRepository.deleteAll()

        val articles = mutableListOf<Article>()
        var longId = 1L
        for (i in 1..100) {
            articles.add(Article(id = longId, data = "content$longId"))
            longId++
        }

        assertThat(articles.size).isEqualTo(100)

        articleRepository.saveAll(articles)

        val latch = CountDownLatch(10)
        for (i in 1..10) {
            service.execute {
                myApiV2()
                latch.countDown()
            }
        }
        latch.await()

        val findAllFalse = articleRepository.findAllByChangeHappened(false)
        assertThat(findAllFalse.size).isEqualTo(0)
        val findAll = articleRepository.findAll()
        assertThat(
            findAll.map { it.readCount }
        ).allMatch { it == 1 }
    }

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional
    @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")])
    fun myApi() {
        val newArticles = articleService.findByChangeHappenedFalse()
        println("newArticles.size: ${newArticles.size}")
        chunkAndProcess(newArticles)
    }

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional
    @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")])
    fun myApiV2() {
        val newArticles = articleService.findByChangeHappenedFalse()
        println("newArticles.size: ${newArticles.size}")
        chunkAndProcessV2(newArticles)
    }

    fun chunkAndProcess(articles: MutableList<Article>) {
        val chunked = articles.chunked(10)

        chunked.parallelStream().forEach { it ->
            it.forEach {
                service.execute {
                    it.changeHappened = true
                    println("it.readCount: ${it.readCount}")
                    it.readCount++
                }
            }
            articleService.save(it)
        }
    }

    fun chunkAndProcessV2(articles: MutableList<Article>) {
        val chunked = articles.chunked(10)

        chunked.parallelStream().forEach { it ->
            it.forEach {
                service.execute {
                    it.changeHappened = true
                    it.readCount++
                    articleService.save(listOf(it))
                    println("it.readCount: ${it.readCount}")

                }
            }
            articleService.save(it)
        }
    }
}

