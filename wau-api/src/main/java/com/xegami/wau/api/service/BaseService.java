package com.xegami.wau.api.service;

import com.xegami.wau.api.domain.Base;
import com.xegami.wau.api.dto.BaseDTO;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
public abstract class BaseService<DOMAIN extends Base, DTO extends BaseDTO> {

    @Autowired
    protected DozerBeanMapper mapper;
    private JpaRepository<DOMAIN, Long> repository;
    private Class<DOMAIN> domainClass;
    private Class<DTO> dtoClass;

    protected BaseService(Class<DOMAIN> domainClass, Class<DTO> dtoClass) {
        this.domainClass = domainClass;
        this.dtoClass = dtoClass;
    }

    protected abstract JpaRepository<DOMAIN, Long> setRepository();

    private void setUp() {
        if (repository == null) {
            repository = setRepository();
        }
    }

    public void saveDTO(DTO dto) {
        setUp();
        DOMAIN domain = mapper.map(dto, domainClass);
        repository.save(domain);
    }

    public void delete(Long id) {
        setUp();
        DOMAIN domain = repository.getOne(id);
        if (domain == null) return;
        repository.delete(domain);
    }

    public DTO getOneDTO(Long id) {
        setUp();
        DOMAIN domain = repository.getOne(id);
        if (domain == null) return null;
        return mapper.map(domain, dtoClass);
    }

    public List<DTO> findAllDTOs() {
        setUp();
        List<DOMAIN> domains = repository.findAll();
        if (domains == null) return null;
        return mapList(domains);
    }

    protected List<DTO> mapList(List<DOMAIN> domains) {
        return domains.stream().map(from -> mapper.map(from, dtoClass)).collect(Collectors.toList());
    }

}
