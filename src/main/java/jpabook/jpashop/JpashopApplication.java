package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication // 하위에 있는 모는 component들을 관리
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
	} // DTO로 client에 노출할 것이기 때문에 사실 크게 쓸모가 없다 -> member : null / orderItems : null이 된다
	// 지연로딩 되게 하는 것은 모두 null로 표시됨

}
