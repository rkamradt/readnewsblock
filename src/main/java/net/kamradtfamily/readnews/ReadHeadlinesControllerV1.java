/*
 * The MIT License
 *
 * Copyright 2020 randalkamradt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.kamradtfamily.readnews;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author randalkamradt
 */
@Slf4j
@RestController
@RequestMapping("/v1/headlines")
public class ReadHeadlinesControllerV1 {
    private static final int MAX_LIMIT = 1000;
    
    private final InsertsRepository newsRepository;
    
    ReadHeadlinesControllerV1(final InsertsRepository newsReactiveRepository) {
        this.newsRepository = newsReactiveRepository;
    }
    
    @GetMapping(path="", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Inserts.Articles> getFromMongo(final Instant from, final Instant to, final Long limit) {
        long actualLimit = limit == null || limit == 0 || limit > MAX_LIMIT 
                ? MAX_LIMIT
                : limit;
        return newsRepository
                .findAll()
                .stream()
                .flatMap(r -> r.getArticles().stream())
                .filter(r -> filterByDate(r, from, to))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    private boolean filterByDate(final Inserts.Articles record, final Instant from, Instant to) {
        if(record == null || record.getPublishedAt() == null) {
            return false;
        }
        Instant theDate;
        try {
            theDate = Instant.parse(record.getPublishedAt());
        } catch(Exception ex) {
            return false;
        }
        return (from == null || theDate.isAfter(from)) &&
            (to == null || theDate.isBefore(to));
    }

}
