package com.saber.springbatchdemo.config;

import com.saber.springbatchdemo.model.Customer;
import com.saber.springbatchdemo.processes.CustomerProcessor;
import com.saber.springbatchdemo.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private final CustomerRepository customerRepository;

    @Bean
    public FlatFileItemReader<Customer> itemReader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(customerLineMapper());
        return itemReader;
    }

    private LineMapper<Customer> customerLineMapper() {
        DefaultLineMapper<Customer> customerLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contractNo"
                , "country", "dob");
        BeanWrapperFieldSetMapper<Customer> customerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        customerFieldSetMapper.setTargetType(Customer.class);

        customerLineMapper.setLineTokenizer(lineTokenizer);
        customerLineMapper.setFieldSetMapper(customerFieldSetMapper);

        return customerLineMapper;
    }

    @Bean
    public CustomerProcessor customerProcessor() {
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> customerRepositoryItemWriter() {
        RepositoryItemWriter<Customer> customerRepositoryItemWriter = new RepositoryItemWriter<>();
        customerRepositoryItemWriter.setRepository(customerRepository);
        return customerRepositoryItemWriter;
    }

    @Bean
    public Step customerStepCsv(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("customerCsvStep", jobRepository)
                .<Customer, Customer>chunk(5, transactionManager)
                .reader(itemReader())
                .processor(customerProcessor())
                .writer(customerRepositoryItemWriter())
                .build();
    }

    @Bean("customerJob")
    public Job customerJob(JobRepository jobRepository, Step customerStepCsv) {
        return new JobBuilder("customerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(customerStepCsv)
                .end().build();
    }

}