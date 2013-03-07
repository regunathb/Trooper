<#assign home_url><@spring.url relativeUrl="${servletPath}/"/></#assign>
<#assign company_url><@spring.messageText code="company.url" text=companyUrl!"https://github.com/regunathb/Trooper"/></#assign>
<#assign company_name><@spring.messageText code="company.name" text=companyName!"Trooper GitHub"/></#assign>
<#assign product_url><@spring.messageText code="product.url" text=productUrl!"https://github.com/regunathb/Trooper"/></#assign>
<#assign product_name><@spring.messageText code="product.name" text=productName!"Trooper Batch"/></#assign>
<div id="primary-navigation">
	<div id="primary-left">
		<ul>
			<#list menuManager.menus as menu>
			<#assign menu_url><@spring.url relativeUrl="${menu.url}"/></#assign>
			<li><a href="${menu_url}">${menu.label}</a></li>
			</#list>
			<li><a href="/configuration/">Job Configuration </a></li>
		</ul>
	</div>
	<div id="primary-right">
		<ul>
			<li><a href="${company_url}">${company_name}</a></li>
		</ul>
	</div>
</div><!-- /primary-navigation -->