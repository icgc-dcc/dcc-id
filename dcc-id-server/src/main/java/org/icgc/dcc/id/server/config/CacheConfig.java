/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.id.server.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.sf.ehcache.config.PersistenceConfiguration.Strategy.LOCALTEMPSWAP;

import javax.management.MBeanServer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.support.MBeanServerFactoryBean;

import lombok.val;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.management.ManagementService;

/**
 * Server wide caching configuration.
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

  @Value("${cache.dir}")
  private String cacheDir;

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager ehCacheManager() {
    val tokenCache = new CacheConfiguration();
    tokenCache.setName("tokens");
    tokenCache.setTimeToLiveSeconds(MINUTES.toSeconds(30));
    tokenCache.setMaxEntriesLocalHeap(100);

    // In-memory caches
    val projectCache = createMemoryCache("projectIds");
    val donorCache = createMemoryCache("donorIds");
    val specimenCache = createMemoryCache("specimenIds");
    val sampleCache = createMemoryCache("sampleIds");

    // Overflow to disk
    val mutationCache = new CacheConfiguration();
    mutationCache.setName("mutationIds");
    mutationCache.setMaxEntriesLocalDisk(0);
    mutationCache.setMaxEntriesLocalHeap(100_000);
    mutationCache.setEternal(true);
    mutationCache.persistence(createPersistenceConfig());

    val config = new net.sf.ehcache.config.Configuration();
    config.addCache(tokenCache);
    config.addCache(projectCache);
    config.addCache(donorCache);
    config.addCache(specimenCache);
    config.addCache(sampleCache);
    config.addCache(mutationCache);
    config.addDiskStore(new DiskStoreConfiguration().path(cacheDir));

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheManager());
  }

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new SimpleKeyGenerator();
  }

  @Bean(initMethod = "init", destroyMethod = "dispose")
  public ManagementService managementService() {
    // Expose the cache manager to JMX
    return new ManagementService(ehCacheManager(), mbeanServer(), true, true, true, true);
  }

  @Bean
  public MBeanServer mbeanServer() {
    val factory = new MBeanServerFactoryBean();
    factory.setLocateExistingServerIfPossible(true);
    factory.afterPropertiesSet();

    return factory.getObject();
  }

  private static CacheConfiguration createMemoryCache(String name) {
    val cache = new CacheConfiguration();
    cache.setName(name);
    cache.setMaxEntriesLocalDisk(0);
    cache.setMaxEntriesLocalHeap(1);
    cache.setEternal(true); // Never expire
    cache.persistence(createPersistenceConfig());

    return cache;
  }

  private static PersistenceConfiguration createPersistenceConfig() {
    return new PersistenceConfiguration().strategy(LOCALTEMPSWAP);
  }

}