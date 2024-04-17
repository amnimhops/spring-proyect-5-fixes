package proyect5.realEstate.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyect5.realEstate.persistence.dtos.InputDTO;
import proyect5.realEstate.persistence.dtos.RentedSurfaceByProvinceDTO;
import proyect5.realEstate.persistence.entity.Flat;
import proyect5.realEstate.persistence.entity.Locality;
import proyect5.realEstate.persistence.entity.Province;
import proyect5.realEstate.persistence.entity.Rent;
import proyect5.realEstate.service.RentedSurfaceByProvinceService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RentedSurfaceByProvinceServiceImpl implements RentedSurfaceByProvinceService {
    @Autowired
    private EntityManager entityManager;
    @Override
    public List<RentedSurfaceByProvinceDTO> generateReport(InputDTO inputDTO) {

        //Obtener los parámetros de entrada desde el objeto InputDTO
        String provinceName = inputDTO.getProvince();
        Date fromDate = inputDTO.getFrom();
        Date toDate = inputDTO.getTo();

        //Crear un CriteriaBuilder para construir la consulta
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Crear un CriteriaQuery con el tipo de resultado deseado
        CriteriaQuery<RentedSurfaceByProvinceDTO> cq = cb.createQuery(RentedSurfaceByProvinceDTO.class);

        //Crear un objeto Root para especificar la entidad principal sobre la que se realizará la consulta
        Root<Rent> rentRoot = cq.from(Rent.class);

        //Realizar las joins necesarias para acceder a las entidades relacionadas
        Join<Rent, Flat> flatJoin = rentRoot.join("flat");
        Join<Flat, Locality> localityJoin = flatJoin.join("locality");
        Join<Locality, Province> provinceJoin = localityJoin.join("province");

        //Construir la consulta para obtener los datos necesarios
        cq.multiselect(
                provinceJoin.get("name"), // Nombre de la provincia
                cb.countDistinct(rentRoot), // Número total de inmuebles alquilados
                cb.sum(flatJoin.get("area")) // Superficie total alquilada
        ).groupBy(provinceJoin.get("name")); // Agrupar por nombre de provincia

        //Aplicar filtros según los parámetros de entrada
        List<Predicate> predicates = new ArrayList<>();
        if (provinceName != null && !provinceName.isEmpty()) {
            predicates.add(cb.equal(cb.lower(provinceJoin.get("name")), provinceName.toLowerCase()));
        }
        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(rentRoot.get("from"), fromDate, toDate));
        }
        cq.where(predicates.toArray(new Predicate[0]));

        //Ejecutar la consulta y obtener los resultados
        TypedQuery<RentedSurfaceByProvinceDTO> query = entityManager.createQuery(cq);
        List<RentedSurfaceByProvinceDTO> report = query.getResultList();

        //Devolver el informe generado
        return report;
    }
}