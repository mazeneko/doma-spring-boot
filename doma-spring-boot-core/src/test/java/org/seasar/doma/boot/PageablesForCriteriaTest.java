package org.seasar.doma.boot;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.seasar.doma.jdbc.criteria.declaration.OrderByNameDeclaration;
import org.seasar.doma.jdbc.criteria.metamodel.EntityMetamodel;
import org.seasar.doma.jdbc.criteria.metamodel.PropertyMetamodel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageablesForCriteriaTest {
	@Test
	public void testOffsetAndLimit() {
		Pageable pageable = PageRequest.of(0, 10);
		Integer offset = PageablesForCriteria.offset(pageable);
		Integer limit = PageablesForCriteria.limit(pageable);
		assertThat(offset, is(0));
		assertThat(limit, is(10));
	}

	@Test
	public void testOffsetAndLimit2() {
		Pageable pageable = PageRequest.of(2, 10);
		Integer offset = PageablesForCriteria.offset(pageable);
		Integer limit = PageablesForCriteria.limit(pageable);
		assertThat(offset, is(20));
		assertThat(limit, is(10));
	}

	@Test
	public void testOffsetAndLimit3() {
		Pageable pageable = PageRequest.of(2, 5);
		Integer offset = PageablesForCriteria.offset(pageable);
		Integer limit = PageablesForCriteria.limit(pageable);
		assertThat(offset, is(10));
		assertThat(limit, is(5));
	}

	@Test
	public void testOffsetAndLimit4() {
		Pageable pageable = Pageable.unpaged();
		Integer offset = PageablesForCriteria.offset(pageable);
		Integer limit = PageablesForCriteria.limit(pageable);
		assertThat(offset, nullValue());
		assertThat(limit, nullValue());
	}

	@Test
	public void testOrderBy() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

		PropertyMetamodel<?> nameProp = mock(PropertyMetamodel.class);

		Consumer<OrderByNameDeclaration> consumer = PageablesForCriteria.orderBy(
				pageable,
				propertyName -> switch (propertyName) {
				case "name" -> Optional.of(nameProp);
				default -> Optional.empty();
				});
		OrderByNameDeclaration orderByNameDeclaration = mock(OrderByNameDeclaration.class);
		consumer.accept(orderByNameDeclaration);
		verify(orderByNameDeclaration, times(1)).asc(nameProp);
	}

	@Test
	public void testOrderBy2() {
		Pageable pageable = PageRequest.of(0, 10,
				Sort.by("name").descending().and(Sort.by("age").ascending()));

		PropertyMetamodel<?> nameProp = mock(PropertyMetamodel.class);
		PropertyMetamodel<?> ageProp = mock(PropertyMetamodel.class);

		Consumer<OrderByNameDeclaration> consumer = PageablesForCriteria.orderBy(
				pageable,
				propertyName -> switch (propertyName) {
				case "name" -> Optional.of(nameProp);
				case "age" -> Optional.of(ageProp);
				default -> Optional.empty();
				});
		OrderByNameDeclaration orderByNameDeclaration = mock(OrderByNameDeclaration.class);
		consumer.accept(orderByNameDeclaration);
		verify(orderByNameDeclaration, times(1)).desc(nameProp);
		verify(orderByNameDeclaration, times(1)).asc(ageProp);
	}

	@Test
	public void testOrderByWhenNonSort() {
		Pageable pageable = PageRequest.of(0, 10);

		Consumer<OrderByNameDeclaration> consumer = PageablesForCriteria.orderBy(
				pageable,
				propertyName -> Optional.empty());
		OrderByNameDeclaration orderByNameDeclaration = mock(OrderByNameDeclaration.class);
		consumer.accept(orderByNameDeclaration);
		verifyNoMoreInteractions(orderByNameDeclaration);
	}

	@Test
	public void testOrderByWhenNonSortAndSetDefault() {
		Pageable pageable = PageRequest.of(0, 10);

		PropertyMetamodel<?> idProp = mock(PropertyMetamodel.class);

		Consumer<OrderByNameDeclaration> consumer = PageablesForCriteria.orderBy(
				pageable,
				propertyName -> Optional.empty(),
				t -> t.asc(idProp));
		OrderByNameDeclaration orderByNameDeclaration = mock(OrderByNameDeclaration.class);
		consumer.accept(orderByNameDeclaration);
		verify(orderByNameDeclaration, times(1)).asc(idProp);
	}

	@Test
	public void testOrderBySingleEntity() {
		Pageable pageable = PageRequest.of(0, 10,
				Sort.by("name").descending().and(Sort.by("age").ascending()));

		PropertyMetamodel<?> nameProp = mock(PropertyMetamodel.class);
		when(nameProp.getName()).thenReturn("name");
		PropertyMetamodel<?> ageProp = mock(PropertyMetamodel.class);
		when(ageProp.getName()).thenReturn("age");
		EntityMetamodel<?> entity = mock(EntityMetamodel.class);
		when(entity.allPropertyMetamodels()).thenReturn(List.of(nameProp, ageProp));

		Consumer<OrderByNameDeclaration> consumer = PageablesForCriteria
				.orderBySingleEntity(pageable, entity);
		OrderByNameDeclaration orderByNameDeclaration = mock(OrderByNameDeclaration.class);
		consumer.accept(orderByNameDeclaration);
		verify(orderByNameDeclaration, times(1)).desc(nameProp);
		verify(orderByNameDeclaration, times(1)).asc(ageProp);
	}
}
