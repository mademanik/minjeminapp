import React from 'react';
import {Menu} from "antd";
import {HomeOutlined, LogoutOutlined, OrderedListOutlined, ProductOutlined, UserOutlined} from '@ant-design/icons'
import {useNavigate} from "react-router-dom";
import keycloak from "../keycloak";

interface MenuListProps {
    darkTheme: boolean;
}

const MenuList: React.FC<MenuListProps> = ({darkTheme}) => {
    const navigate = useNavigate();

    return (
        <Menu theme={darkTheme ? 'dark' : 'light'} mode="inline" className="menu-bar">
            <Menu.Item key="home" icon={<HomeOutlined/>} onClick={() => navigate("/dashboard")}>
                Home
            </Menu.Item>
            <Menu.Item key="products" icon={<ProductOutlined/>} onClick={() => navigate("/dashboard/products")}>
                Products
            </Menu.Item>
            <Menu.Item key="rentals" icon={<OrderedListOutlined/>} onClick={() => navigate("/dashboard/rentals")}>
                Rentals
            </Menu.Item>
            <Menu.Item key="signout" icon={<LogoutOutlined/>} onClick={() => keycloak.logout()}>
                Sign Out
            </Menu.Item>
        </Menu>
    );
};

export default MenuList;