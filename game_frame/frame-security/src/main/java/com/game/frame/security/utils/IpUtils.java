package com.game.frame.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * IP工具类
 * 提供IP解析、地理位置、代理检测功能
 * @author lx
 * @date 2025/06/08
 */
@Component
public class IpUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);
    
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );
    
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );
    
    // 私有IP地址范围
    private static final List<String> PRIVATE_IP_RANGES = Arrays.asList(
        "10.0.0.0/8",
        "172.16.0.0/12",
        "192.168.0.0/16",
        "127.0.0.0/8",
        "169.254.0.0/16"
    );
    
    // 已知代理服务器标识
    private static final List<String> PROXY_INDICATORS = Arrays.asList(
        "proxy", "cache", "squid", "tor", "vpn"
    );
    
    /**
     * 验证IPv4地址格式
     */
    public boolean isValidIPv4(String ip) {
        return ip != null && IPV4_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 验证IPv6地址格式
     */
    public boolean isValidIPv6(String ip) {
        return ip != null && IPV6_PATTERN.matcher(ip).matches();
    }
    
    /**
     * 验证IP地址格式
     */
    public boolean isValidIP(String ip) {
        return isValidIPv4(ip) || isValidIPv6(ip);
    }
    
    /**
     * 检查是否为私有IP地址
     */
    public boolean isPrivateIP(String ip) {
        if (!isValidIPv4(ip)) {
            return false;
        }
        
        try {
            long ipLong = ipToLong(ip);
            
            // 10.0.0.0/8 (10.0.0.0 - 10.255.255.255)
            if (ipLong >= ipToLong("10.0.0.0") && ipLong <= ipToLong("10.255.255.255")) {
                return true;
            }
            
            // 172.16.0.0/12 (172.16.0.0 - 172.31.255.255)
            if (ipLong >= ipToLong("172.16.0.0") && ipLong <= ipToLong("172.31.255.255")) {
                return true;
            }
            
            // 192.168.0.0/16 (192.168.0.0 - 192.168.255.255)
            if (ipLong >= ipToLong("192.168.0.0") && ipLong <= ipToLong("192.168.255.255")) {
                return true;
            }
            
            // 127.0.0.0/8 (127.0.0.0 - 127.255.255.255) - Loopback
            if (ipLong >= ipToLong("127.0.0.0") && ipLong <= ipToLong("127.255.255.255")) {
                return true;
            }
            
            // 169.254.0.0/16 (169.254.0.0 - 169.254.255.255) - Link-local
            if (ipLong >= ipToLong("169.254.0.0") && ipLong <= ipToLong("169.254.255.255")) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check private IP: {}", ip, e);
            return false;
        }
    }
    
    /**
     * 检查是否为本地IP
     */
    public boolean isLocalIP(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }
        
        return "127.0.0.1".equals(ip) || 
               "localhost".equals(ip) || 
               "0:0:0:0:0:0:0:1".equals(ip) || 
               "::1".equals(ip);
    }
    
    /**
     * 获取IP地理位置信息（简化版）
     */
    public GeoLocation getGeoLocation(String ip) {
        if (!isValidIP(ip)) {
            return new GeoLocation("Unknown", "Unknown", "Unknown", 0.0, 0.0);
        }
        
        // 本地和私有IP返回本地信息
        if (isLocalIP(ip) || isPrivateIP(ip)) {
            return new GeoLocation("Local", "Local", "Local", 0.0, 0.0);
        }
        
        // TODO: 集成第三方IP地理位置服务（如MaxMind GeoIP、IP2Location等）
        // 这里提供一个简化的模拟实现
        return simulateGeoLocation(ip);
    }
    
    /**
     * 模拟地理位置查询（用于演示）
     */
    private GeoLocation simulateGeoLocation(String ip) {
        try {
            // 根据IP段简单判断地区
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return new GeoLocation("Unknown", "Unknown", "Unknown", 0.0, 0.0);
            }
            
            int firstOctet = Integer.parseInt(parts[0]);
            
            // 简化的地区判断逻辑
            if (firstOctet >= 1 && firstOctet <= 60) {
                return new GeoLocation("China", "Beijing", "CN", 39.9042, 116.4074);
            } else if (firstOctet >= 61 && firstOctet <= 120) {
                return new GeoLocation("United States", "New York", "US", 40.7128, -74.0060);
            } else if (firstOctet >= 121 && firstOctet <= 180) {
                return new GeoLocation("Japan", "Tokyo", "JP", 35.6762, 139.6503);
            } else {
                return new GeoLocation("Europe", "London", "GB", 51.5074, -0.1278);
            }
            
        } catch (Exception e) {
            logger.error("Failed to simulate geo location for IP: {}", ip, e);
            return new GeoLocation("Unknown", "Unknown", "Unknown", 0.0, 0.0);
        }
    }
    
    /**
     * 检查是否为代理IP
     */
    public boolean isProxyIP(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }
        
        try {
            // 反向DNS查询
            InetAddress address = InetAddress.getByName(ip);
            String hostname = address.getCanonicalHostName();
            
            if (!ip.equals(hostname)) {
                // 检查主机名是否包含代理相关关键词
                String lowerHostname = hostname.toLowerCase();
                for (String indicator : PROXY_INDICATORS) {
                    if (lowerHostname.contains(indicator)) {
                        logger.debug("Proxy detected via hostname: {} -> {}", ip, hostname);
                        return true;
                    }
                }
            }
            
            // TODO: 其他代理检测方法
            // 1. 检查已知代理IP数据库
            // 2. 检查代理端口开放情况
            // 3. 检查HTTP代理头信息
            
            return false;
            
        } catch (UnknownHostException e) {
            // 无法解析主机名，不一定是代理
            logger.debug("Cannot resolve hostname for IP: {}", ip);
            return false;
        } catch (Exception e) {
            logger.error("Failed to check proxy IP: {}", ip, e);
            return false;
        }
    }
    
    /**
     * 检查IP是否在指定网段内
     */
    public boolean isIPInRange(String ip, String cidr) {
        try {
            if (!isValidIPv4(ip)) {
                return false;
            }
            
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }
            
            String networkIP = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkIP);
            
            // 计算网络掩码
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
            
            return (ipLong & mask) == (networkLong & mask);
            
        } catch (Exception e) {
            logger.error("Failed to check IP range: {} in {}", ip, cidr, e);
            return false;
        }
    }
    
    /**
     * 计算两个IP之间的距离（简化版）
     */
    public double calculateDistance(String ip1, String ip2) {
        try {
            GeoLocation geo1 = getGeoLocation(ip1);
            GeoLocation geo2 = getGeoLocation(ip2);
            
            return calculateDistance(geo1.getLatitude(), geo1.getLongitude(),
                                   geo2.getLatitude(), geo2.getLongitude());
                                   
        } catch (Exception e) {
            logger.error("Failed to calculate distance between IPs: {} and {}", ip1, ip2, e);
            return -1;
        }
    }
    
    /**
     * 计算地理坐标距离（哈弗赛因公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * IP转换为长整型
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += Long.parseLong(parts[i]) << (24 - i * 8);
        }
        return result & 0xFFFFFFFFL;
    }
    
    /**
     * 长整型转换为IP
     */
    public String longToIP(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }
    
    /**
     * 获取IP的网络部分
     */
    public String getNetworkAddress(String ip, int prefixLength) {
        try {
            if (!isValidIPv4(ip)) {
                return null;
            }
            
            long ipLong = ipToLong(ip);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
            long networkLong = ipLong & mask;
            
            return longToIP(networkLong);
            
        } catch (Exception e) {
            logger.error("Failed to get network address for IP: {}/{}", ip, prefixLength, e);
            return null;
        }
    }
    
    /**
     * 地理位置信息类
     */
    public static class GeoLocation {
        private String country;
        private String city;
        private String countryCode;
        private double latitude;
        private double longitude;
        
        public GeoLocation(String country, String city, String countryCode, double latitude, double longitude) {
            this.country = country;
            this.city = city;
            this.countryCode = countryCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // Getters and setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        @Override
        public String toString() {
            return String.format("GeoLocation{country='%s', city='%s', countryCode='%s', latitude=%.4f, longitude=%.4f}",
                    country, city, countryCode, latitude, longitude);
        }
    }
}