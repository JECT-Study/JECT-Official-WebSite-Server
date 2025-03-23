package org.ject.support.common.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Configuration
@RequiredArgsConstructor
public class ObjectMapperConfig {
    private final PageResponseSerializer pageResponseSerializer;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper(SimpleModule redisSimpleModule) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(redisSimpleModule);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean("pageObjectMapper")
    public ObjectMapper pageObjectMapper(SimpleModule simpleModule) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(simpleModule);
        return mapper;
    }

    @Bean
    public SimpleModule simpleModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Page.class, pageResponseSerializer);
        return module;
    }

    @Bean
    public SimpleModule redisSimpleModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new PageResponseDeserializer());
        return module;
    }
}
