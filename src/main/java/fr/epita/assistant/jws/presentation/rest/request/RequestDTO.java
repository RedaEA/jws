package fr.epita.assistant.jws.presentation.rest.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@AllArgsConstructor @NoArgsConstructor
public class RequestDTO {
        public String name;
}
