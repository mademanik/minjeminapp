import React from 'react';
import {Menu} from "antd";
import {HomeOutlined, LogoutOutlined, OrderedListOutlined, ProductOutlined, FormOutlined} from '@ant-design/icons'
import {useNavigate} from "react-router-dom";
import {useKeycloak} from "@react-keycloak/web";

interface MenuListProps {
    darkTheme: boolean;
}

const MenuList: React.FC<MenuListProps> = ({darkTheme}) => {
    const {keycloak} = useKeycloak();
    const isAdmin = keycloak?.hasRealmRole("admin-role");
    const navigate = useNavigate();

    return (
        <Menu theme={darkTheme ? 'dark' : 'light'} mode="inline" className="menu-bar">
            {isAdmin && (
                <Menu.Item key="home" icon={<HomeOutlined/>} onClick={() => navigate("/dashboard")}>
                    Home
                </Menu.Item>
            )}
            <Menu.Item key="products" icon={<ProductOutlined/>} onClick={() => navigate("/dashboard/products")}>
                Products
            </Menu.Item>
            <Menu.Item key="rentals" icon={<OrderedListOutlined/>} onClick={() => navigate("/dashboard/rentals")}>
                My Rentals
            </Menu.Item>
            <Menu.Item key="request-rentals" icon={<FormOutlined/>} onClick={() => navigate("/dashboard/request-rentals")}>
                Request Rentals
            </Menu.Item>
            <Menu.Item key="signout" icon={<LogoutOutlined/>} onClick={() => keycloak.logout()}>
                Sign Out
            </Menu.Item>
        </Menu>
    );
};

export default MenuList;