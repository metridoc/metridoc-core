/*
 * Copyright 2010 Trustees of the University of Pennsylvania Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package metridoc.plugins.table.core

@Mixin(DatasourceCategory)
class LimitableWrapper implements LimitableDatasource, DatasourceWrapper{
	//Delegate to the underlying wrapper
	@Delegate Datasource d

	public LimitableWrapper(d){
		this.d=d
	}

	public setDatasource(d){
		this.d.datasource=d
	}	

	public getDatasource(){
		d.datasource
	}	

	public Object each(Object builder,Object closure){
		def offset = 0
		def limit = 0
		//Ignore everything if countable is true...
		if(builder.options && builder.options.countResults){
			return d.each(builder,closure)
		}

		d.each(builder){row->
			if(builder.options && builder.options.offset!=null && offset < builder.options.offset){
				offset++
				return //Don't execute the closure
			}

			if(builder.options && builder.options.limit!=null){
				if(limit < builder.options.limit){
					limit++
					closure(row)
				}else{
					return //Don't execute the closure
				}
			}else{
				closure(row)
			}
		}
	}
}
