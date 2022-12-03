package com.lime.batchwithdatabase.config;

import com.lime.batchwithdatabase.entity.Person;
import com.lime.batchwithdatabase.listener.JobCompletionNotificationListener;
import com.lime.batchwithdatabase.repository.MyPersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class MyBatchConfig {

    // Since JobBuilderFactory is deprecated, so use the new one

    @Autowired
    public JobBuilderFactory jobBuilder;

    @Autowired
    public StepBuilderFactory stepBuilder;

    @Bean
    public Job importPersonJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilder.get("importPerson")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilder.get("step1")
                .<Person, Person>chunk(10)
                .reader(myReader())
                .processor(myProcessor())
                .writer(myWriter())
                .build();
    }

    private MyPersonRepository personRepository;

    @Bean
    public FlatFileItemReader<Person> myReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("/Users/lime/IdeaProjects/BatchWithDataBase/src/main/resources/people-100.csv"));
        reader.setName("csvReader");
        // Skip the head row in csv file
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper());
        return reader;
    }

    // Map csv to Java Object
    private LineMapper<Person> lineMapper() {
        DefaultLineMapper<Person> mapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(";");

        // Map the head row
        tokenizer.setNames("id", "user_id", "first_name", "last_name", "sex", "job_title");

        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Person.class);

        mapper.setFieldSetMapper(fieldSetMapper);
        mapper.setLineTokenizer(tokenizer);

        return mapper;

    }

    @Bean
    public PersonProcessor myProcessor() {
        return new PersonProcessor();
    }

    @Bean
    public RepositoryItemWriter<Person> myWriter() {
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(personRepository);
        writer.setMethodName("save");
        return writer;
    }

}
